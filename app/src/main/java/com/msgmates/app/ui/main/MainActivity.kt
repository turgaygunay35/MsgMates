package com.msgmates.app.ui.main

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.annotation.AnimRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.msgmates.app.BuildConfig
import com.msgmates.app.R
import com.msgmates.app.core.auth.AccessToken
import com.msgmates.app.core.auth.AuthGuard
import com.msgmates.app.core.auth.AuthTokens
import com.msgmates.app.core.auth.AutoRefreshLifecycle
import com.msgmates.app.core.auth.RefreshToken
import com.msgmates.app.core.auth.SessionEvents
import com.msgmates.app.core.auth.SessionStarter
import com.msgmates.app.core.auth.TokenRepository
import com.msgmates.app.core.datastore.AuthTokenStore
import com.msgmates.app.core.env.EnvConfig
import com.msgmates.app.core.network.EchoApi
import com.msgmates.app.core.ui.UIEnhancements
import com.msgmates.app.databinding.ActivityMainBinding
import com.msgmates.app.ui.chats.ChatsViewModel
import com.msgmates.app.ui.contacts.ContactsViewModel
import com.msgmates.app.ui.journal.JournalViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch
import retrofit2.Retrofit

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var vb: ActivityMainBinding
    private lateinit var nav: NavController
    private lateinit var chatsViewModel: ChatsViewModel
    private lateinit var contactsViewModel: ContactsViewModel
    private lateinit var journalViewModel: JournalViewModel

    @Inject
    lateinit var tokenRepository: TokenRepository

    @Inject
    lateinit var authTokenStore: AuthTokenStore

    @Inject
    lateinit var sessionStarter: SessionStarter

    @Inject
    lateinit var autoRefreshLifecycle: AutoRefreshLifecycle

    @Inject
    lateinit var envConfig: EnvConfig

    @Inject
    lateinit var retrofit: Retrofit

    @Inject
    lateinit var authGuard: AuthGuard

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ZORUNLU: Auth kontrolü yap - Tek Giriş Noktası
        android.util.Log.d("MainActivity", "Checking auth...")
        val isAuthenticated = authGuard.isAuthenticated()
        android.util.Log.d("MainActivity", "Is authenticated: $isAuthenticated")

        if (!isAuthenticated) {
            android.util.Log.d("MainActivity", "Not authenticated, redirecting to auth")
            authGuard.redirectToAuth(this)
            return // Auth'a yönlendirildi, bu activity'yi kapat
        }

        android.util.Log.d("MainActivity", "Authenticated, starting main flow")

        // Auth başarılı, Main akışını başlat
        // MainGraph'taki TÜM ekranlar bu noktadan sonra güvenli

        vb = ActivityMainBinding.inflate(layoutInflater)
        setContentView(vb.root)

        // ViewModel'leri başlat
        chatsViewModel = ViewModelProvider(this)[ChatsViewModel::class.java]
        contactsViewModel = ViewModelProvider(this)[ContactsViewModel::class.java]
        journalViewModel = ViewModelProvider(this)[JournalViewModel::class.java]

        // ÖNEMLİ: activity_main.xml'deki id "nav_host"
        val host = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        nav = host.navController

        // BottomNavigationView setup
        vb.bottomBar.setupWithNavController(nav)

        // Apply UI enhancements
        applyUIEnhancements()

        // Sekme izolasyonu: sadece aktif sekme görünür
        vb.bottomBar.setOnItemReselectedListener { _ ->
            nav.popBackStack(nav.graph.startDestinationId, false)
        }

        // Sekme değişiminde fragment lifecycle'ını kontrol et
        nav.addOnDestinationChangedListener { _, destination, _ ->
            handleTabChange(destination.id)
        }
        val sel = savedInstanceState?.getInt("bottom_selected") ?: R.id.dest_chats
        vb.bottomBar.selectedItemId = sel

        // AppBar'ı sadece chats ekranında göster
        setupAppBarVisibility()
        setupFilterChips()
        setupUnreadCount()
        setupSearchButton()
        setupRefreshButton()

        // Auth kontrolü ve sessiz yenileme
        checkAuthStatus()

        // Auto refresh lifecycle
        lifecycle.addObserver(autoRefreshLifecycle)

        // Global logout handling
        setupGlobalLogout()

        // Debug test - sadece debug build'de çalışır
        if (BuildConfig.DEBUG) {
            runAuthDebugTest()
        }
    }

    private fun setupAppBarVisibility() {
        nav.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.dest_chats -> {
                    // Sohbetler: Sadece arama ikonu
                    vb.ivLogo.visibility = View.VISIBLE
                    vb.tvTitle.visibility = View.VISIBLE
                    vb.statusCapsuleContainer.visibility = View.VISIBLE
                    vb.appbarContainer.visibility = View.VISIBLE
                    vb.searchContainer.visibility = View.VISIBLE
                    vb.btnQuickAction.visibility = View.GONE
                    vb.etSearch.hint = "Sohbetlerde ara"
                }
                R.id.dest_contacts -> {
                    // Rehber: Arama ikonu + Yenile butonu
                    vb.ivLogo.visibility = View.VISIBLE
                    vb.tvTitle.visibility = View.VISIBLE
                    vb.statusCapsuleContainer.visibility = View.VISIBLE
                    vb.appbarContainer.visibility = View.GONE
                    vb.searchContainer.visibility = View.VISIBLE
                    vb.btnQuickAction.visibility = View.GONE
                    vb.btnRefresh.visibility = View.VISIBLE
                    vb.etSearch.hint = "Rehberde ara"
                }
                R.id.dest_journal -> {
                    // Günlük: Sadece + ikonu
                    vb.ivLogo.visibility = View.VISIBLE
                    vb.tvTitle.visibility = View.VISIBLE
                    vb.statusCapsuleContainer.visibility = View.VISIBLE
                    vb.appbarContainer.visibility = View.GONE
                    vb.searchContainer.visibility = View.GONE
                    vb.btnQuickAction.visibility = View.VISIBLE
                    vb.btnRefresh.visibility = View.GONE
                }
                R.id.dest_menu -> {
                    // Menü: Hiçbir ikon
                    vb.ivLogo.visibility = View.VISIBLE
                    vb.tvTitle.visibility = View.VISIBLE
                    vb.statusCapsuleContainer.visibility = View.VISIBLE
                    vb.appbarContainer.visibility = View.GONE
                    vb.searchContainer.visibility = View.GONE
                    vb.btnQuickAction.visibility = View.GONE
                    vb.btnRefresh.visibility = View.GONE
                }
                R.id.dest_disaster_mode -> {
                    // Afet Modu: Kırmızı tema, sadece geri ok
                    vb.ivLogo.visibility = View.VISIBLE
                    vb.tvTitle.visibility = View.VISIBLE
                    vb.tvTitle.text = "🚨 Afet Modu"
                    vb.statusCapsuleContainer.visibility = View.GONE
                    vb.appbarContainer.visibility = View.VISIBLE
                    vb.searchContainer.visibility = View.GONE
                    vb.btnQuickAction.visibility = View.GONE
                    vb.btnRefresh.visibility = View.GONE
                    // Kırmızı tema uygula
                    vb.appbarContainer.setBackgroundColor(getColor(R.color.disaster_red))
                    vb.tvTitle.setTextColor(getColor(android.R.color.white))
                }
                else -> {
                    // Diğer ekranlar: Sadece geri ok
                    vb.ivLogo.visibility = View.VISIBLE
                    vb.tvTitle.visibility = View.VISIBLE
                    vb.statusCapsuleContainer.visibility = View.VISIBLE
                    vb.appbarContainer.visibility = View.GONE
                    vb.searchContainer.visibility = View.GONE
                    vb.btnQuickAction.visibility = View.GONE
                    vb.btnRefresh.visibility = View.GONE
                }
            }
        }
    }

    private fun setupFilterChips() {
        // Filter chip'leri ayarla
        val chips = listOf(
            vb.chipFilterAll to "all",
            vb.chipFilterUnread to "unread", vb.chipFilterGroups to "groups",
            vb.chipFilterFavorites to "favorites",
            vb.chipFilterArchived to "archived",
            vb.chipFilterDisaster to "disaster"
        )

        chips.forEach { (chip, filter) ->
            chip.setOnClickListener {
                // Güvenli chip scale animasyonu
                safeStartAnim(chip, R.anim.chip_scale_up)

                // Tüm chip'leri seçili olmayan duruma getir
                chips.forEach { (c, _) ->
                    c.isChecked = false
                }
                // Tıklanan chip'i seçili yap
                chip.isChecked = true

                // ViewModel'e filter değişikliğini bildir
                chatsViewModel.selectFilter(filter)
            }
        }

        // İlk chip'i seçili yap ve yeşil renk yap
        vb.chipFilterAll.isChecked = true
        vb.chipFilterAll.setChipBackgroundColorResource(R.color.primary_green_light)
        vb.chipFilterAll.setChipStrokeColorResource(R.color.primary_green)
    }

    private fun setupUnreadCount() {
        // Okunmamış mesaj sayısını gözlemle
        lifecycleScope.launch {
            chatsViewModel.unreadCount.collect { count ->
                // Okunmamış chip'ine badge ekle
                if (count > 0) {
                    vb.chipFilterUnread.text = "Okunmamış ($count)"
                } else {
                    vb.chipFilterUnread.text = "Okunmamış"
                }
            }
        }
    }

    fun updateAppBarElevation(isScrolled: Boolean) {
        val elevation = if (isScrolled) 8f else 2f
        vb.appbarContainer.elevation = elevation
    }

    private fun setupSearchButton() {
        vb.btnSearch.setOnClickListener {
            // Arama kutusunu göster/gizle
            val isVisible = vb.searchContainer.visibility == View.VISIBLE
            vb.searchContainer.visibility = if (isVisible) View.GONE else View.VISIBLE

            if (!isVisible) {
                // Arama kutusu açıldığında focus ver
                vb.etSearch.requestFocus()
                val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.showSoftInput(vb.etSearch, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
            }
        }

        // + İkonu için click listener
        vb.btnQuickAction.setOnClickListener {
            when (nav.currentDestination?.id) {
                R.id.dest_chats -> {
                    // Sohbet menüsünü göster
                    showChatsQuickActionsMenu()
                }
                R.id.dest_journal -> {
                    // Günlük ekleme fragment'ine git
                    nav.navigate(R.id.dest_journal_add)
                }
            }
        }

        // Arama filtreleme - fragment'e özel
        vb.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val query = s.toString().trim()
                // Mevcut fragment'e göre arama yap
                when (nav.currentDestination?.id) {
                    R.id.dest_chats -> {
                        // ChatsViewModel'e arama query'sini gönder
                        chatsViewModel.searchConversations(query)
                    }
                    R.id.dest_contacts -> {
                        // ContactsViewModel'e arama query'sini gönder
                        contactsViewModel.searchContacts(query)
                    }
                    // Menu fragment'te arama yok
                }
            }
        })

        // Hızlı aksiyon butonu
        vb.btnQuickAction.setOnClickListener {
            showQuickActionsBottomSheet()
        }
    }

    private fun setupRefreshButton() {
        vb.btnRefresh.setOnClickListener {
            // Contacts sekmesindeyken yenile butonuna basıldığında
            when (nav.currentDestination?.id) {
                R.id.dest_contacts -> {
                    // ContactsViewModel'e yenileme isteği gönder
                    contactsViewModel.refreshContacts()
                }
            }
        }
    }

    private fun showChatsQuickActionsMenu() {
        val menuItems = arrayOf(
            "Grup Oluştur",
            "Toplu Sil", 
            "Not Ekle"
        )

        MaterialAlertDialogBuilder(this)
            .setTitle("Sohbet İşlemleri")
            .setItems(menuItems) { _, which ->
                when (which) {
                    0 -> {
                        // Grup Oluştur
                        navigateToCreateGroup()
                    }
                    1 -> {
                        // Toplu Sil - ChatsFragment'e sinyal gönder
                        enableMultiSelectMode()
                    }
                    2 -> {
                        // Not Ekle
                        showAddNoteDialog()
                    }
                }
            }
            .show()
    }

    private fun showQuickActionsBottomSheet() {
        val bottomSheet = QuickActionsBottomSheet.newInstance()

        bottomSheet.setOnDailyShareClickListener {
            // Günlük paylaş ekranına git
            navigateToDailyShare()
        }

        bottomSheet.setOnCreateGroupClickListener {
            // Grup kur ekranına git
            navigateToCreateGroup()
        }

        bottomSheet.show(supportFragmentManager, "QuickActionsBottomSheet")
    }

    private fun navigateToDailyShare() {
        nav.navigate(R.id.dest_daily_share)
    }

    private fun navigateToCreateGroup() {
        nav.navigate(R.id.dest_create_group)
    }

    private fun enableMultiSelectMode() {
        // ChatsFragment'e çoklu seçim modunu aktif etmesi için sinyal gönder
        // Bu işlem ChatsFragment'te handle edilecek
        // TODO: ChatsFragment ile iletişim kurulacak
    }

    private fun showAddNoteDialog() {
        val input = android.widget.EditText(this)
        input.hint = "Notunuzu yazın..."
        input.setPadding(50, 30, 50, 30)

        MaterialAlertDialogBuilder(this)
            .setTitle("Not Ekle")
            .setView(input)
            .setPositiveButton("Kaydet") { _, _ ->
                val noteText = input.text.toString().trim()
                if (noteText.isNotEmpty()) {
                    // Notu kaydet ve Menü → Notlar bölümüne gönder
                    saveNote(noteText)
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun saveNote(noteText: String) {
        // TODO: Notu kaydet ve Menü → Notlar bölümüne gönder
        // Bu işlem Notes modülü ile entegre edilecek
        android.widget.Toast.makeText(this, "Not kaydedildi: $noteText", android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun checkAuthStatus() {
        lifecycleScope.launch {
            val tokens = tokenRepository.getTokens()
            if (tokens.access == null) {
                // Token yok, OTP phone entry ekranına git
                nav.navigate(R.id.dest_phone_entry)
            } else {
                // Token var, sessiz yenileme dene
                if (!sessionStarter.ensureFreshSession()) {
                    // Sessiz yenileme başarısız, global logout tetiklenecek
                    android.util.Log.w("MainActivity", "Silent refresh failed, will trigger global logout")
                }
            }
        }
    }

    private fun setupGlobalLogout() {
        lifecycleScope.launchWhenStarted {
            SessionEvents.forceLogout.collect {
                // Global logout tetiklendi
                authTokenStore.clearTokens()
                nav.navigate(R.id.dest_phone_entry)
            }
        }
    }

    private fun runAuthDebugTest() {
        lifecycleScope.launch {
            try {
                android.util.Log.d("AuthTest", "=== AUTH DEBUG TEST START ===")

                // Env config test
                android.util.Log.d("AuthTest", "Base URL: ${envConfig.baseUrl}")
                android.util.Log.d("AuthTest", "WS URL: ${envConfig.wsUrl}")
                android.util.Log.d("AuthTest", "Auth loginStart: ${envConfig.auth.loginStart}")

                // Token test
                val now = System.currentTimeMillis() / 1000
                val fakeTokens = AuthTokens(
                    access = AccessToken("fakeAccessToken123", now + 3600),
                    refresh = RefreshToken("fakeRefreshToken456", now + 86400)
                )

                android.util.Log.d("AuthTest", "Saving fake tokens...")
                tokenRepository.setTokens("fakeAccessToken123", "fakeRefreshToken456")

                val retrievedTokens = tokenRepository.getTokens()
                android.util.Log.d("AuthTest", "Retrieved access token: ${retrievedTokens.access}")
                android.util.Log.d("AuthTest", "Retrieved refresh token: ${retrievedTokens.refresh}")

                // Clear test
                android.util.Log.d("AuthTest", "Clearing tokens...")
                tokenRepository.clear()

                val clearedTokens = tokenRepository.getTokens()
                android.util.Log.d(
                    "AuthTest",
                    "After clear - access: ${clearedTokens.access}, refresh: ${clearedTokens.refresh}"
                )

                // Network test
                android.util.Log.d("AuthTest", "Testing network with fake token...")
                tokenRepository.setTokens("fakeAccessToken123", "fakeRefreshToken456")

                val echoApi = retrofit.create(EchoApi::class.java)
                runCatching { echoApi.echo().code() }.onSuccess { code ->
                    android.util.Log.d("AuthTest", "Echo API response code: $code")
                }.onFailure { error ->
                    android.util.Log.e("AuthTest", "Echo API failed", error)
                }

                android.util.Log.d("AuthTest", "=== AUTH DEBUG TEST COMPLETE ===")
            } catch (e: Exception) {
                android.util.Log.e("AuthTest", "Auth debug test failed", e)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("bottom_selected", vb.bottomBar.selectedItemId)
    }

    override fun onBackPressed() {
        // MainActivity'de geri tuşu uygulamadan çıkış yapsın
        // (Login ekranına dönmesin)
        finishAffinity()
    }

    /**
     * Sekme değişiminde fragment lifecycle'ını kontrol et
     * Sadece aktif sekme görünür, diğerleri gizli
     */
    private fun handleTabChange(destinationId: Int) {
        when (destinationId) {
            R.id.dest_chats -> {
                // Chats sekmesi aktif
                showAppBar(true)
                hideOtherTabs()
            }
            R.id.dest_contacts -> {
                // Contacts sekmesi aktif
                showAppBar(false)
                hideOtherTabs()
            }
            R.id.dest_journal -> {
                // Journal sekmesi aktif
                showAppBar(false)
                hideOtherTabs()
            }
            R.id.dest_menu -> {
                // Menu sekmesi aktif
                showAppBar(false)
                hideOtherTabs()
            }
        }
    }

    /**
     * AppBar'ı göster/gizle
     */
    private fun showAppBar(show: Boolean) {
        // AppBar visibility kontrolü
        // vb.appBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    /**
     * Diğer sekmeleri gizle (sadece aktif sekme görünür)
     */
    private fun hideOtherTabs() {
        // Fragment container'da sadece aktif fragment görünür
        // Navigation Component otomatik olarak bunu yönetir
    }

    /**
     * Güvenli animasyon başlatma
     */
    private fun safeStartAnim(view: View, @androidx.annotation.AnimRes resId: Int) {
        runCatching { AnimationUtils.loadAnimation(this, resId) }.onSuccess { view.startAnimation(it) }.onFailure {
            // Fallback: ViewPropertyAnimator
            view.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(100)
                .withEndAction {
                    view.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(100)
                        .start()
                }
                .start()
        }
    }

    /**
     * Apply UI enhancements for better user experience
     */
    private fun applyUIEnhancements() {
        // Apply touch feedback to bottom navigation items
        for (i in 0 until vb.bottomBar.menu.size()) {
            val item = vb.bottomBar.menu.getItem(i)
            val view = vb.bottomBar.findViewById<View>(item.itemId)
            view?.let { UIEnhancements.applyTouchFeedback(it) }
        }

        // Apply system window insets for edge-to-edge display
        UIEnhancements.applySystemWindowInsets(vb.root)

        // Apply ripple effect to clickable elements
        vb.bottomBar.let { UIEnhancements.applyRippleEffect(it) }
    }
}
