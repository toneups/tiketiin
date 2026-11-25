package com.example.tiketiin

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tiketiin.data.model.EventCategory
import com.example.tiketiin.data.model.OrderStatus
import com.example.tiketiin.data.repository.DefaultTiketiinRepository
import com.example.tiketiin.data.source.DummyDataSource
import com.example.tiketiin.ui.auth.LoginScreen
import com.example.tiketiin.ui.auth.OtpScreen
import com.example.tiketiin.ui.auth.RegisterScreen
import com.example.tiketiin.ui.auth.WelcomeScreen
import com.example.tiketiin.ui.checkout.CheckoutViewModel
import com.example.tiketiin.ui.checkout.OrderFormScreen
import com.example.tiketiin.ui.checkout.PaymentScreen
import com.example.tiketiin.ui.checkout.PaymentSuccessScreen
import com.example.tiketiin.ui.checkout.PaymentSuccessViewModel
import com.example.tiketiin.ui.components.BottomNavBar
import com.example.tiketiin.ui.detail.DetailScreen
import com.example.tiketiin.ui.detail.DetailViewModel
import com.example.tiketiin.ui.foryou.ForYouScreen
import com.example.tiketiin.ui.foryou.ForYouViewModel
import com.example.tiketiin.ui.home.HomeScreen
import com.example.tiketiin.ui.home.HomeViewModel
import com.example.tiketiin.ui.orders.OrdersScreen
import com.example.tiketiin.ui.orders.OrdersViewModel
import com.example.tiketiin.ui.orders.TicketDetailScreen
import com.example.tiketiin.ui.orders.TicketViewModel
import com.example.tiketiin.ui.profile.ProfileScreen
import com.example.tiketiin.ui.profile.ProfileViewModel
import com.example.tiketiin.ui.saved.SavedScreen
import com.example.tiketiin.ui.saved.SavedViewModel
import com.example.tiketiin.ui.search.SearchScreen
import com.example.tiketiin.ui.search.SearchViewModel
import com.example.tiketiin.ui.sell.EkycScreen
import com.example.tiketiin.ui.sell.HotOpportunityListScreen
import com.example.tiketiin.ui.sell.HotOpportunityViewModel
import com.example.tiketiin.ui.sell.ListingPublishedScreen
import com.example.tiketiin.ui.sell.PriceCheckScreen
import com.example.tiketiin.ui.sell.SellFormScreen
import com.example.tiketiin.ui.sell.SellScreen
import com.example.tiketiin.ui.sell.SellViewModel
import com.example.tiketiin.ui.seller.ManageEventsScreen
import com.example.tiketiin.ui.seller.ManageEventsViewModel
import com.example.tiketiin.ui.seller.MyListingsScreen
import com.example.tiketiin.ui.seller.MyListingsViewModel
import com.example.tiketiin.ui.seller.SalesReportScreen
import com.example.tiketiin.ui.seller.SalesViewModel
import com.example.tiketiin.ui.simpleFactory
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import com.example.tiketiin.data.repository.RepositoryProvider
import android.widget.Toast

/**
 * Akar aplikasi: NavHost + bottom navigation (perannya sama seperti NavigationLabApp
 * di tugas navigation).
 */
@Composable
fun TiketiinApp() {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    // Sumber data sementara masih dummy. Nanti cukup ganti di baris ini.
    val repository = remember { DefaultTiketiinRepository(DummyDataSource) }

    val context = LocalContext.current.applicationContext

    val eventRepository = remember(context) {
        RepositoryProvider.getEventRepository(context)
    }

    // ViewModel yang dipakai lintas layar dibuat di sini agar datanya bertahan
    // selama berpindah langkah (checkout, alur jual, dan tab pesanan).
    val checkoutViewModel: CheckoutViewModel = viewModel(
        factory = simpleFactory { CheckoutViewModel(repository) }
    )
    val sellViewModel: SellViewModel = viewModel(
        factory = simpleFactory { SellViewModel(repository) }
    )
    val ordersViewModel: OrdersViewModel = viewModel(
        factory = simpleFactory { OrdersViewModel(repository) }
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    // Nomor HP dari layar daftar, ditampilkan lagi di layar OTP.
    val registeredPhone = remember { mutableStateOf("") }
    val startDestination = remember {
        if (DummyDataSource.isLoggedIn) Routes.HOME else Routes.WELCOME
    }

    // Pindah tab bawah tanpa menumpuk layar.
    fun openTab(route: String) {
        navController.navigate(route) {
            popUpTo(Routes.HOME) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    // Selesai login/daftar: masuk ke Beranda dan buang layar autentikasi.
    fun enterApp() {
        scope.launch { repository.login() }
        navController.navigate(Routes.HOME) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    fun openEvent(eventId: Int) {
        navController.navigate(Routes.detail(eventId))
    }

    // Mulai checkout (dari Detail Event atau Event Tersimpan).
    fun startCheckout(eventId: Int, quantity: Int) {
        checkoutViewModel.start(eventId, quantity)
        navController.navigate(Routes.ORDER_FORM)
    }

    // Mulai alur jual: e-KYC dulu kalau akun belum terverifikasi.
    fun startSelling(eventId: Int) {
        sellViewModel.startSelling(eventId) { verified ->
            navController.navigate(if (verified) Routes.SELL_FORM else Routes.EKYC)
        }
    }

    Scaffold(
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (currentRoute in Routes.TABS) {
                BottomNavBar(currentRoute = currentRoute, onSelect = { openTab(it) })
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {

            // ------------------------- AUTENTIKASI -------------------------
            composable(Routes.WELCOME) {
                WelcomeScreen(
                    onRegister = { navController.navigate(Routes.REGISTER) },
                    onLogin = { navController.navigate(Routes.LOGIN) }
                )
            }

            composable(Routes.LOGIN) {
                LoginScreen(
                    onBack = { navController.popBackStack() },
                    onLoggedIn = { enterApp() },
                    onRegister = { navController.navigate(Routes.REGISTER) }
                )
            }

            composable(Routes.REGISTER) {
                RegisterScreen(
                    onBack = { navController.popBackStack() },
                    onRegistered = { phone ->
                        registeredPhone.value = phone
                        navController.navigate(Routes.OTP)
                    },
                    onLogin = { navController.navigate(Routes.LOGIN) }
                )
            }

            composable(Routes.OTP) {
                OtpScreen(
                    phone = registeredPhone.value,
                    onBack = { navController.popBackStack() },
                    onVerified = { enterApp() }
                )
            }

            // ------------------------- TAB: BERANDA -------------------------
            composable(Routes.HOME) {

                val viewModel: HomeViewModel = viewModel(
                    factory = simpleFactory {
                        HomeViewModel(eventRepository)
                    }
                )

                val uiState by
                viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    viewModel.load()
                }

                HomeScreen(
                    uiState = uiState,

                    onSearch = {
                        Toast.makeText(
                            context,
                            "Integrasi pencarian belum selesai",
                            Toast.LENGTH_SHORT
                        ).show()
                    },

                    onOpenSaved = {
                        Toast.makeText(
                            context,
                            "Integrasi wishlist belum selesai",
                            Toast.LENGTH_SHORT
                        ).show()
                    },

                    onOpenEvent = {
                        Toast.makeText(
                            context,
                            "Integrasi detail event belum selesai",
                            Toast.LENGTH_SHORT
                        ).show()
                    },

                    onSelectCategory = viewModel::selectCategory,
                    onRetry = viewModel::load
                )
            }

            // ------------------------- TAB: UNTUKMU (Fungsi 3) -------------------------
            composable(Routes.FOR_YOU) {
                val viewModel: ForYouViewModel = viewModel(
                    factory = simpleFactory { ForYouViewModel(repository) }
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) { viewModel.load() }

                ForYouScreen(
                    uiState = uiState,
                    onSelectTab = viewModel::selectTab,
                    onSelectCategory = viewModel::selectCategory,
                    onSearch = { navController.navigate(Routes.search()) },
                    onOpenSaved = { navController.navigate(Routes.SAVED) },
                    onOpenEvent = { openEvent(it) },
                    onRetry = viewModel::load
                )
            }

            // ------------------------- TAB: JUAL TIKET (Fungsi 2) -------------------------
            composable(Routes.SELL) {
                val viewModel: HotOpportunityViewModel = viewModel(
                    factory = simpleFactory { HotOpportunityViewModel(repository) }
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) { viewModel.load() }

                SellScreen(
                    uiState = uiState,
                    onSeeAll = { navController.navigate(Routes.HOT_LIST) },
                    onSellTicket = { startSelling(it) },
                    onOpenEvent = { openEvent(it) },
                    onRetry = viewModel::load,
                    onManageEvents = { navController.navigate(Routes.MANAGE_EVENTS) },
                    onMyListings = { navController.navigate(Routes.MY_LISTINGS) },
                    onSalesReport = { navController.navigate(Routes.SALES_REPORT) }
                )
            }

            composable(Routes.HOT_LIST) {
                val viewModel: HotOpportunityViewModel = viewModel(
                    factory = simpleFactory { HotOpportunityViewModel(repository) }
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) { viewModel.load() }

                HotOpportunityListScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onToggleFilter = viewModel::toggleFilter,
                    onSellTicket = { startSelling(it) },
                    onOpenEvent = { openEvent(it) },
                    onRetry = viewModel::load
                )
            }

            // ------------------------- TAB: PESANAN -------------------------
            composable(Routes.ORDERS) {
                val uiState by ordersViewModel.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) { ordersViewModel.load() }

                OrdersScreen(
                    uiState = uiState,
                    onSelectTab = ordersViewModel::selectTab,
                    onSelectStatus = ordersViewModel::selectStatus,
                    onMoveTo = ordersViewModel::moveTo,
                    onRate = ordersViewModel::rate,
                    onOpenTicket = { navController.navigate(Routes.ticket(it)) }
                )
            }

            composable(
                route = Routes.TICKET,
                arguments = listOf(navArgument("orderId") { type = NavType.IntType })
            ) { entry ->
                val orderId = entry.arguments?.getInt("orderId") ?: -1
                val viewModel: TicketViewModel = viewModel(
                    factory = simpleFactory { TicketViewModel(repository) }
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(orderId) { viewModel.load(orderId) }

                TicketDetailScreen(uiState = uiState, onBack = { navController.popBackStack() })
            }

            // ------------------------- TAB: PROFIL -------------------------
            composable(Routes.PROFILE) {
                val viewModel: ProfileViewModel = viewModel(
                    factory = simpleFactory { ProfileViewModel(repository) }
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) { viewModel.load() }

                ProfileScreen(
                    uiState = uiState,
                    onOpenSaved = { navController.navigate(Routes.SAVED) },
                    onLogout = {
                        viewModel.logout {
                            navController.navigate(Routes.WELCOME) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        }
                    }
                )
            }

            // ------------------------- CARI & TERSIMPAN -------------------------
            composable(
                route = Routes.SEARCH,
                arguments = listOf(navArgument("category") { type = NavType.StringType })
            ) { entry ->
                val categoryName = entry.arguments?.getString("category") ?: "all"
                val initialCategory = EventCategory.entries.find { it.name == categoryName }
                val viewModel: SearchViewModel = viewModel(
                    factory = simpleFactory { SearchViewModel(repository, initialCategory) }
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                SearchScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onQueryChange = viewModel::updateQuery,
                    onSelectCategory = viewModel::selectCategory,
                    onToggleSort = viewModel::toggleSort,
                    onToggleSaved = viewModel::toggleSaved,
                    onOpenEvent = { openEvent(it) },
                    onRetry = viewModel::refresh
                )
            }

            composable(Routes.SAVED) {
                val viewModel: SavedViewModel = viewModel(
                    factory = simpleFactory { SavedViewModel(repository) }
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) { viewModel.load() }

                SavedScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onSelectCategory = viewModel::selectCategory,
                    onToggleSelected = viewModel::toggleSelected,
                    onRemove = viewModel::remove,
                    onOpenEvent = { openEvent(it) },
                    onCheckout = { eventId -> startCheckout(eventId, 1) }
                )
            }

            // ------------------------- DETAIL EVENT & CHECKOUT -------------------------
            composable(
                route = Routes.DETAIL,
                arguments = listOf(navArgument("eventId") { type = NavType.IntType })
            ) { entry ->
                val eventId = entry.arguments?.getInt("eventId") ?: -1
                val viewModel: DetailViewModel = viewModel(
                    factory = simpleFactory { DetailViewModel(repository) }
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(eventId) { viewModel.load(eventId) }

                DetailScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onSelectTab = viewModel::selectTab,
                    onChangeQuantity = viewModel::changeQuantity,
                    onToggleSaved = viewModel::toggleSaved,
                    onBuy = { id, quantity -> startCheckout(id, quantity) }
                )
            }

            composable(Routes.ORDER_FORM) {
                val uiState by checkoutViewModel.uiState.collectAsStateWithLifecycle()
                OrderFormScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onNameChange = checkoutViewModel::updateName,
                    onEmailChange = checkoutViewModel::updateEmail,
                    onPhoneChange = checkoutViewModel::updatePhone,
                    onIdNumberChange = checkoutViewModel::updateIdNumber,
                    onAddressChange = checkoutViewModel::updateAddress,
                    onTogglePowerOfAttorney = checkoutViewModel::togglePowerOfAttorney,
                    onContinue = {
                        if (checkoutViewModel.validateForm()) {
                            navController.navigate(Routes.PAYMENT)
                        }
                    }
                )
            }

            composable(Routes.PAYMENT) {
                val uiState by checkoutViewModel.uiState.collectAsStateWithLifecycle()
                PaymentScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onSelectMethod = checkoutViewModel::selectPaymentMethod,
                    onPay = {
                        checkoutViewModel.pay { orderId ->
                            // Setelah bayar, layar checkout dibuang dari tumpukan.
                            navController.navigate(Routes.paymentSuccess(orderId)) {
                                popUpTo(Routes.HOME)
                            }
                        }
                    }
                )
            }

            // Pembayaran berhasil + Rekomendasi Hotel (Fungsi 4)
            composable(
                route = Routes.PAYMENT_SUCCESS,
                arguments = listOf(navArgument("orderId") { type = NavType.IntType })
            ) { entry ->
                val orderId = entry.arguments?.getInt("orderId") ?: -1
                val viewModel: PaymentSuccessViewModel = viewModel(
                    factory = simpleFactory { PaymentSuccessViewModel(repository) }
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(orderId) { viewModel.load(orderId) }

                val goToOrders = {
                    ordersViewModel.selectTab(0)
                    ordersViewModel.selectStatus(OrderStatus.DIPROSES)
                    openTab(Routes.ORDERS)
                }
                PaymentSuccessScreen(
                    uiState = uiState,
                    onBack = { goToOrders() },
                    onViewOrders = { goToOrders() }
                )
            }

            // ------------------------- ALUR JUAL TIKET -------------------------
            composable(Routes.EKYC) {
                val uiState by sellViewModel.uiState.collectAsStateWithLifecycle()
                EkycScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onToggleKtp = sellViewModel::toggleKtp,
                    onToggleSelfie = sellViewModel::toggleSelfie,
                    onNameChange = sellViewModel::updateKycName,
                    onNikChange = sellViewModel::updateKycNik,
                    onSubmit = {
                        sellViewModel.submitKyc {
                            navController.navigate(Routes.SELL_FORM) {
                                popUpTo(Routes.EKYC) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable(Routes.SELL_FORM) {
                val uiState by sellViewModel.uiState.collectAsStateWithLifecycle()
                SellFormScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onSelectEvent = sellViewModel::selectEvent,
                    onSeatCategoryChange = sellViewModel::updateSeatCategory,
                    onRowChange = sellViewModel::updateRow,
                    onQuantityChange = sellViewModel::updateQuantity,
                    onFaceValueChange = sellViewModel::updateFaceValue,
                    onPriceChange = sellViewModel::updatePrice,
                    onToggleProof = sellViewModel::toggleProof,
                    onPickProof = { bytes, mime, name -> sellViewModel.uploadProof(bytes, mime, name) },
                    onSubmit = {
                        sellViewModel.submitForm { navController.navigate(Routes.PRICE_CHECK) }
                    }
                )
            }

            // Cek Harga Wajar (Fungsi 1)
            composable(Routes.PRICE_CHECK) {
                val uiState by sellViewModel.uiState.collectAsStateWithLifecycle()
                PriceCheckScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onPublish = { price ->
                        sellViewModel.publish(price) {
                            navController.navigate(Routes.LISTING_PUBLISHED) {
                                popUpTo(Routes.SELL)
                            }
                        }
                    }
                )
            }

            composable(Routes.LISTING_PUBLISHED) {
                val uiState by sellViewModel.uiState.collectAsStateWithLifecycle()
                ListingPublishedScreen(
                    uiState = uiState,
                    onViewStoreOrders = {
                        ordersViewModel.selectTab(1)
                        ordersViewModel.selectStatus(OrderStatus.DIPROSES)
                        openTab(Routes.ORDERS)
                    },
                    onViewListings = {
                        navController.navigate(Routes.MY_LISTINGS) {
                            popUpTo(Routes.SELL)
                        }
                    }
                )
            }

            // ------------------------- MENU PENJUAL -------------------------
            composable(Routes.MANAGE_EVENTS) {
                val viewModel: ManageEventsViewModel = viewModel(
                    factory = simpleFactory { ManageEventsViewModel(repository) }
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) { viewModel.load() }

                ManageEventsScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    formatDate = viewModel::formatDate,
                    onSave = viewModel::save,
                    onDelete = viewModel::delete,
                    onClearError = viewModel::clearError
                )
            }

            composable(Routes.MY_LISTINGS) {
                val viewModel: MyListingsViewModel = viewModel(
                    factory = simpleFactory { MyListingsViewModel(repository) }
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) { viewModel.load() }

                MyListingsScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onUpdatePrice = viewModel::updatePrice,
                    onDelete = viewModel::delete,
                    onClearError = viewModel::clearError,
                    onAddListing = { startSelling(-1) }
                )
            }

            composable(Routes.SALES_REPORT) {
                val viewModel: SalesViewModel = viewModel(
                    factory = simpleFactory { SalesViewModel(repository) }
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) { viewModel.load() }

                SalesReportScreen(uiState = uiState, onBack = { navController.popBackStack() })
            }
        }
    }
}
