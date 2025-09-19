package com.msgmates.app.ui.chats

import com.msgmates.app.BuildConfig as AppBuildConfig
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.msgmates.app.R
import com.msgmates.app.core.analytics.EventLogger
import com.msgmates.app.core.auth.AuthGuard
import javax.inject.Inject
import com.msgmates.app.databinding.FragmentChatsBinding
// import com.msgmates.app.databinding.ViewStatusCapsuleBinding // Artık MainActivity'de
import com.msgmates.app.databinding.ViewStateEmptyBinding
import com.msgmates.app.databinding.ViewStateErrorBinding
import com.msgmates.app.domain.chats.Conversation
import com.msgmates.app.ui.common.StatusCapsuleUi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest

/**
 * Main Chats screen displaying conversation list with filters, search functionality,
 * status capsule, and disaster mode integration.
 */
@AndroidEntryPoint
class ChatsFragment : Fragment() {
    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!

    // StatusCapsule artık MainActivity'de yönetiliyor
    // private var _statusCapsuleBinding: ViewStatusCapsuleBinding? = null
    // private val statusCapsuleBinding get() = _statusCapsuleBinding!!

    internal val viewModel: ChatsViewModel by viewModels()

    @Inject
    lateinit var authGuard: AuthGuard

    private val savedStateHandle: SavedStateHandle by lazy {
        SavedStateHandle()
    }

    private lateinit var chatsAdapter: ChatsAdapter
    private var isSelectionMode = false

    private var _emptyStateBinding: ViewStateEmptyBinding? = null
    private val emptyStateBinding get() = _emptyStateBinding!!

    private var _errorStateBinding: ViewStateErrorBinding? = null
    private val errorStateBinding get() = _errorStateBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // AUTH ZORUNLU: Fragment seviyesinde auth kontrolü
        if (!authGuard.requireAuth(requireContext())) {
            return // Auth'a yönlendirildi
        }

        try {
            setupRecyclerView()
            setupMenu()
            setupStatusCapsule()
            setupFilterChips()
            setupSearchMode()
            setupBackPressedCallback()
            setupDailyBar()
            observeConnectionStatus()
            observeUnreadCount()
            observeDisasterMode()
            observeConversationsState()
        } catch (t: Throwable) {
            android.util.Log.e("CrashGuard", "ChatsFragment onViewCreated failed", t)
            toast("Beklenmeyen bir hata oluştu")
        }
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvChats.layoutManager = layoutManager

        // Performance optimizations
        binding.rvChats.setHasFixedSize(true)
        binding.rvChats.setItemViewCacheSize(20) // Cache more views for smooth scrolling
        binding.rvChats.setDrawingCacheEnabled(true)
        binding.rvChats.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH)

        // Additional performance optimizations
        binding.rvChats.setRecycledViewPool(
            androidx.recyclerview.widget.RecyclerView.RecycledViewPool().apply {
                setMaxRecycledViews(0, 20) // Cache more views of the same type
            }
        )

        // Enable nested scrolling for better performance with AppBarLayout
        binding.rvChats.isNestedScrollingEnabled = true

        // AppBar elevation değişimi için scroll listener
        binding.rvChats.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val isScrolled = recyclerView.canScrollVertically(-1) // Yukarı scroll edilebilir mi?
                (requireActivity() as? com.msgmates.app.ui.main.MainActivity)?.updateAppBarElevation(isScrolled)
            }
        })

        // Setup adapter
        chatsAdapter = ChatsAdapter()
        binding.rvChats.adapter = chatsAdapter

        // Setup click listeners
        chatsAdapter.setOnItemClickListener { conversation ->
            if (isSelectionMode) {
                chatsAdapter.toggleSelection(conversation)
            } else {
                navigateToChatDetail(conversation.id)
            }
        }

        chatsAdapter.setOnItemLongClickListener { conversation ->
            if (!isSelectionMode) {
                enterSelectionMode()
                chatsAdapter.toggleSelection(conversation)
            }
            true
        }

        // Setup selection mode change listener
        chatsAdapter.setOnSelectionModeChangeListener { selectionMode ->
            isSelectionMode = selectionMode
            updateToolbarForSelectionMode(selectionMode)
        }

        // Setup overflow click listener
        chatsAdapter.setOnOverflowClickListener { conversation ->
            showMessageOptionsBottomSheet(conversation)
        }

        // Setup swipe actions
        setupSwipeActions()

        // Observe paged data
        observeConversations()
    }

    private fun setupSwipeActions() {
        val swipeActionHelper = SwipeActionHelper(
            onArchive = { conversation ->
                viewModel.archiveConversation(conversation)
                toast("Arşivlendi: ${conversation.title}")
            },
            onMute = { conversation ->
                viewModel.muteConversation(conversation)
                val action = if (conversation.isMuted) "Sessizden çıkarıldı" else "Sessize alındı"
                toast("$action: ${conversation.title}")
            }
        )

        val itemTouchHelper = ItemTouchHelper(swipeActionHelper)
        itemTouchHelper.attachToRecyclerView(binding.rvChats)
    }

    private fun observeConversations() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.pagedConversations.collectLatest { pagingData ->
                chatsAdapter.submitData(pagingData)
            }
        }
    }

    private fun navigateToChatDetail(conversationId: String) {
        val bundle = android.os.Bundle().apply {
            putString("conversationId", conversationId)
        }
        findNavController().navigate(R.id.action_chats_to_chat_detail, bundle)
    }

    private fun enterSelectionMode() {
        isSelectionMode = true
        chatsAdapter.enterSelectionMode()
        updateToolbarForSelectionMode(true)
    }

    internal fun exitSelectionMode() {
        isSelectionMode = false
        chatsAdapter.exitSelectionMode()
        updateToolbarForSelectionMode(false)
    }

    private fun updateToolbarForSelectionMode(selectionMode: Boolean) {
        // AppBar artık MainActivity'de, burada sadece selection mode state'ini yönetiyoruz
        if (selectionMode) {
            // Selection mode aktif - gerekirse MainActivity'deki AppBar'ı güncelleyebiliriz
            // Şimdilik sadece adapter state'ini yönetiyoruz
        } else {
            // Normal mode - AppBar normal haline döner
        }
    }

    private fun deleteSelectedConversations() {
        val selectedItems = chatsAdapter.getSelectedItems()
        if (selectedItems.isNotEmpty()) {
            // Show confirmation dialog
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Sohbetleri Sil")
                .setMessage("${selectedItems.size} sohbet silinecek. Bu işlem geri alınamaz.")
                .setPositiveButton("Sil") { _, _ ->
                    viewModel.deleteConversations(selectedItems)
                    exitSelectionMode()
                    toast("${selectedItems.size} sohbet silindi")
                }
                .setNegativeButton("İptal", null)
                .show()
        }
    }

    private fun archiveSelectedConversations() {
        val selectedItems = chatsAdapter.getSelectedItems()
        if (selectedItems.isNotEmpty()) {
            viewModel.archiveConversations(selectedItems)
            exitSelectionMode()
            toast("${selectedItems.size} sohbet arşivlendi")
        }
    }

    private fun muteSelectedConversations() {
        val selectedItems = chatsAdapter.getSelectedItems()
        if (selectedItems.isNotEmpty()) {
            viewModel.muteConversations(selectedItems)
            exitSelectionMode()
            toast("${selectedItems.size} sohbet sessize alındı")
        }
    }

    private fun selectAllConversations() {
        // Select all visible conversations
        for (i in 0 until chatsAdapter.itemCount) {
            val conversation = chatsAdapter.getItemAt(i)
            if (conversation != null && !chatsAdapter.isSelected(conversation)) {
                chatsAdapter.toggleSelection(conversation)
            }
        }
        updateToolbarForSelectionMode(true)
    }

    private fun deselectAllConversations() {
        chatsAdapter.exitSelectionMode()
        updateToolbarForSelectionMode(false)
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_chats_topappbar, menu)

                    // Hide QA menu item in release builds
                    if (!AppBuildConfig.FF_QA_SCREEN) {
                        menu.removeItem(R.id.menu_qa)
                    }
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.action_search -> {
                            EventLogger.logSearchEnter()
                            viewModel.enterSearch()
                            true
                        }
                        R.id.action_new -> {
                            EventLogger.logAppBarClick("new")
                            // Hızlı aksiyon artık MainActivity'de
                            true
                        }
                        R.id.action_overflow_root -> {
                            EventLogger.logMenuOpen("overflow")
                            // TODO: Show overflow menu
                            true
                        }
                        R.id.menu_create_group -> {
                            EventLogger.logMenuOpen("create_group")
                            findNavController().navigate(R.id.dest_create_group)
                            true
                        }
                        R.id.menu_devices -> {
                            EventLogger.logMenuOpen("devices")
                            // COMMENTED OUT FOR CLEAN BUILD
                            // findNavController().navigate(R.id.devicesFragment)
                            true
                        }
                        R.id.menu_starred -> {
                            EventLogger.logMenuOpen("starred")
                            // COMMENTED OUT FOR CLEAN BUILD
                            // findNavController().navigate(R.id.starredMessagesFragment)
                            true
                        }
                        R.id.menu_archive -> {
                            EventLogger.logMenuOpen("archive")
                            // COMMENTED OUT FOR CLEAN BUILD
                            // findNavController().navigate(R.id.archiveFragment)
                            true
                        }
                        R.id.menu_settings -> {
                            EventLogger.logMenuOpen("settings")
                            // COMMENTED OUT FOR CLEAN BUILD
                            // findNavController().navigate(R.id.settingsFragment)
                            true
                        }
                        R.id.menu_switch_account -> {
                            EventLogger.logMenuOpen("switch_account")
                            // COMMENTED OUT FOR CLEAN BUILD
                            // findNavController().navigate(R.id.switchAccountFragment)
                            true
                        }
                        R.id.menu_help_feedback -> {
                            EventLogger.logMenuOpen("help_feedback")
                            // COMMENTED OUT FOR CLEAN BUILD
                            // findNavController().navigate(R.id.helpFeedbackFragment)
                            true
                        }
                        R.id.menu_about -> {
                            EventLogger.logMenuOpen("about")
                            // COMMENTED OUT FOR CLEAN BUILD
                            // findNavController().navigate(R.id.aboutFragment)
                            true
                        }
                        R.id.menu_qa -> {
                            if (AppBuildConfig.FF_QA_SCREEN) {
                                EventLogger.logMenuOpen("qa")
                                // COMMENTED OUT FOR CLEAN BUILD
                                // findNavController().navigate(R.id.qaFragment)
                            }
                            true
                        }
                        else -> false
                    }
                }
            },
            viewLifecycleOwner, Lifecycle.State.RESUMED
        )
    }

    private fun setupStatusCapsule() {
        // Status capsule artık MainActivity'de, burada sadece observer'ları kuruyoruz
        // MainActivity'deki status capsule'ı güncellemek için callback kullanabiliriz
    }

    private fun setupFilterChips() {
        // Filter chips artık MainActivity'de, burada sadece observer'ları kuruyoruz
        // MainActivity'deki filter chips'leri güncellemek için callback kullanabiliriz
    }

    private fun createFilterChips(visibleFilters: List<String>) {
        // Filter chips artık MainActivity'de yönetiliyor
        // Bu method artık kullanılmıyor
    }

    private fun getChipId(filterId: String): Int {
        return when (filterId) {
            "all" -> R.id.chip_filter_all
            "unread" -> R.id.chip_filter_unread
            "groups" -> R.id.chip_filter_groups
            "favorites" -> R.id.chip_filter_favorites
            "archived" -> R.id.chip_filter_archived
            "disaster" -> R.id.chip_filter_disaster
            else -> View.generateViewId()
        }
    }

    private fun getFilterLabel(filterId: String): String {
        return when (filterId) {
            "all" -> "Tümü"
            "unread" -> "Okunmamış"
            "groups" -> "Gruplar"
            "favorites" -> "Favoriler"
            "archived" -> "Arşiv"
            "disaster" -> "Afet"
            else -> filterId
        }
    }

    private fun updateSelectedChip(selectedFilter: String) {
        // Filter chips artık MainActivity'de yönetiliyor
        // Bu method artık kullanılmıyor
    }

    private fun showFilterOptionsBottomSheet(filterId: String, visibleFilters: List<String>) {
        val currentOrder = viewModel.filterOrder.value
        val currentIndex = currentOrder.indexOf(filterId)
        val isHidden = viewModel.hiddenFilters.value.contains(filterId)
        val canMoveUp = currentIndex > 0
        val canMoveDown = currentIndex < currentOrder.size - 1

        val bottomSheet = FilterOptionsBottomSheet.newInstance(
            filterId = filterId,
            isHidden = isHidden,
            canMoveUp = canMoveUp,
            canMoveDown = canMoveDown
        )

        bottomSheet.setOnMoveUpListener {
            viewModel.moveFilterUp(filterId)
        }

        bottomSheet.setOnMoveDownListener {
            viewModel.moveFilterDown(filterId)
        }

        bottomSheet.setOnToggleVisibilityListener {
            viewModel.toggleFilterVisibility(filterId)
        }

        bottomSheet.show(parentFragmentManager, "FilterOptions")
    }

    private fun observeConnectionStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.connectionUi.collectLatest { statusUi ->
                updateStatusCapsule(statusUi)
            }
        }
    }

    private fun observeUnreadCount() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.unreadCount.collectLatest { count ->
                updateUnreadChipBadge(count)
            }
        }
    }

    private fun updateStatusCapsule(statusUi: StatusCapsuleUi) {
        // StatusCapsule artık MainActivity'de yönetiliyor
        // Bu method artık kullanılmıyor
    }

    private fun updateUnreadChipBadge(count: Int) {
        // Unread badge artık MainActivity'de yönetiliyor
        // Bu method artık kullanılmıyor
    }

    private fun observeDisasterMode() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isDisasterMode.collectLatest { isDisasterMode ->
                updateDisasterModeUI(isDisasterMode)
            }
        }
    }

    private fun updateDisasterModeUI(isDisasterMode: Boolean) {
        // StatusCapsule artık MainActivity'de yönetiliyor
        // Bu method artık kullanılmıyor
    }

    private fun updateDisasterChip(isDisasterMode: Boolean) {
        // Disaster chip artık MainActivity'de yönetiliyor
        // Bu method artık kullanılmıyor
    }

    private fun observeConversationsState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.conversationsState.collect { state ->
                when (state) {
                    is ConversationsState.Loading -> {
                        showLoadingState()
                    }
                    is ConversationsState.Empty -> {
                        showEmptyState()
                    }
                    is ConversationsState.Error -> {
                        showErrorState(state.throwable)
                    }
                }
            }
        }
    }

    private fun showLoadingState() {
        binding.rvChats.visibility = View.VISIBLE
        hideEmptyState()
        hideErrorState()
    }

    private fun showEmptyState() {
        binding.rvChats.visibility = View.GONE
        hideErrorState()
        showEmptyStateView()
    }

    private fun showErrorState(throwable: Throwable) {
        binding.rvChats.visibility = View.GONE
        hideEmptyState()
        showErrorStateView(throwable)
    }

    private fun showEmptyStateView() {
        if (_emptyStateBinding == null) {
            _emptyStateBinding = ViewStateEmptyBinding.inflate(layoutInflater)
            binding.root.addView(emptyStateBinding.root)

            emptyStateBinding.btnEmptyAction.setOnClickListener {
                // TODO: Navigate to new chat
                toast("Yeni sohbet başlat")
            }
        }
        emptyStateBinding.root.visibility = View.VISIBLE
    }

    private fun hideEmptyState() {
        _emptyStateBinding?.root?.visibility = View.GONE
    }

    private fun showErrorStateView(throwable: Throwable) {
        if (_errorStateBinding == null) {
            _errorStateBinding = ViewStateErrorBinding.inflate(layoutInflater)
            binding.root.addView(errorStateBinding.root)

            errorStateBinding.btnRetry.setOnClickListener {
                viewModel.retryLoadConversations()
            }
        }
        errorStateBinding.root.visibility = View.VISIBLE
    }

    private fun hideErrorState() {
        _errorStateBinding?.root?.visibility = View.GONE
    }

    private fun setupSearchMode() {
        // Observe search mode changes
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isSearchMode.collect { isSearchMode ->
                if (isSearchMode) {
                    enterSearchMode()
                } else {
                    exitSearchMode()
                }
            }
        }

        // Observe search query from MainActivity
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchQuery.collect { query ->
                // Search query değiştiğinde filtreleme yap
                viewModel.searchConversations(query)
            }
        }
    }

    private fun setupBackPressedCallback() {
        val callback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                if (isSelectionMode) {
                    exitSelectionMode()
                } else if (viewModel.isSearchMode.value) {
                    viewModel.exitSearch()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        // Enable/disable callback based on search mode or selection mode
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isSearchMode.collect { isSearchMode ->
                callback.isEnabled = isSearchMode || isSelectionMode
            }
        }
    }

    private fun enterSearchMode() {
        // Search container artık MainActivity'de, burada sadece state'i yönetiyoruz
        // MainActivity'deki search container'ı güncellemek için callback kullanabiliriz
    }

    private fun exitSearchMode() {
        // Search container artık MainActivity'de, burada sadece state'i yönetiyoruz
        // MainActivity'deki search container'ı güncellemek için callback kullanabiliriz
    }

    // Geçici olarak seçim modu devre dışı
    /*
    private fun enterSelectionMode() {
        isSelectionMode = true

        // TalkBack announcement
        binding.root.announceForAccessibility(getString(R.string.announce_selection_mode))

        // Change toolbar to selection mode
        binding.toolbarChats.setNavigationIcon(R.drawable.ic_arrow_back)
        binding.toolbarChats.setNavigationOnClickListener {
            exitSelectionMode()
        }

        // Inflate selection menu
        binding.toolbarChats.menu.clear()
        binding.toolbarChats.inflateMenu(R.menu.menu_chats_selection)

        // Setup selection menu click listeners
        setupSelectionMenuListeners()

        // Update selection UI
        updateSelectionUI()
    }

    private fun exitSelectionMode() {
        isSelectionMode = false

        // TalkBack announcement
        binding.root.announceForAccessibility(getString(R.string.announce_exit_selection_mode))

        // Restore normal toolbar
        binding.toolbarChats.setNavigationIcon(null)
        binding.toolbarChats.setNavigationOnClickListener(null)

        // Restore normal menu
        binding.toolbarChats.menu.clear()
        binding.toolbarChats.inflateMenu(R.menu.menu_chats_topappbar)

        // Restore normal title
        binding.toolbarChats.title = ""
    }

    private fun setupSelectionMenuListeners() {
        binding.toolbarChats.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete -> {
                    handleDelete()
                    true
                }
                R.id.action_mark_read -> {
                    handleMarkRead()
                    true
                }
                R.id.action_mute -> {
                    handleMute()
                    true
                }
                R.id.action_share -> {
                    handleShare()
                    true
                }
                else -> false
            }
        }
    }

    private fun updateSelectionUI() {
        binding.toolbarChats.title = getString(R.string.sel_count, 1) // Basit sayım
    }

    private fun handleDelete() {
        toast(getString(R.string.sel_deleted))
        exitSelectionMode()
    }

    private fun handleMarkRead() {
        toast(getString(R.string.sel_marked_read))
        exitSelectionMode()
    }

    private fun handleMute() {
        toast(getString(R.string.sel_muted))
        exitSelectionMode()
    }

    private fun handleShare() {
        toast(getString(R.string.sel_shared))
        exitSelectionMode()
    }
    */

    private fun showMessageOptionsBottomSheet(conversation: Conversation) {
        val bottomSheet = MessageOptionsBottomSheet.newInstance(conversation)

        bottomSheet.setOnMarkReadListener {
            // TODO: Implement mark as read
            toast("Okundu olarak işaretlendi: ${conversation.title}")
        }

        bottomSheet.setOnMuteListener {
            viewModel.muteConversation(conversation)
            val action = if (conversation.isMuted) "Sessizden çıkarıldı" else "Sessize alındı"
            toast("$action: ${conversation.title}")
        }

        bottomSheet.setOnStarListener {
            // TODO: Implement star/unstar
            toast("Favorilere eklendi: ${conversation.title}")
        }

        bottomSheet.setOnArchiveListener {
            viewModel.archiveConversation(conversation)
            toast("Arşivlendi: ${conversation.title}")
        }

        bottomSheet.setOnDeleteListener {
            // Show confirmation dialog
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Sohbeti Sil")
                .setMessage("${conversation.title} silinecek. Bu işlem geri alınamaz.")
                .setPositiveButton("Sil") { _, _ ->
                    viewModel.deleteConversation(conversation)
                    toast("Silindi: ${conversation.title}")
                }
                .setNegativeButton("İptal", null)
                .show()
        }

        bottomSheet.show(parentFragmentManager, "MessageOptions")
    }

    private fun setupDailyBar() {
        // Günlük bar'ı kapat butonu
        binding.btnDailyBarClose.setOnClickListener {
            binding.cardDailyBar.visibility = View.GONE
        }

        // TODO: Günlük paylaşımları kontrol et ve günlük bar'ı göster
        // Şimdilik test için göstermiyoruz
        binding.cardDailyBar.visibility = View.GONE
    }

    private fun showDailyBar() {
        binding.cardDailyBar.visibility = View.VISIBLE
    }

    private fun hideDailyBar() {
        binding.cardDailyBar.visibility = View.GONE
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // _statusCapsuleBinding = null // Artık MainActivity'de
        _emptyStateBinding = null
        _errorStateBinding = null
    }
}
