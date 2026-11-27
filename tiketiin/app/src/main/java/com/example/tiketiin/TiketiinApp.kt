package com.example.tiketiin

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import com.example.tiketiin.data.auth.AuthRepository
import com.example.tiketiin.data.auth.SessionManager
import com.example.tiketiin.data.local.DatabaseProvider
import com.example.tiketiin.data.local.HotelLocalDataSource
import com.example.tiketiin.data.local.OrderLocalDataSource
import com.example.tiketiin.data.model.EventCategory
import com.example.tiketiin.data.model.OrderStatus
import com.example.tiketiin.data.repository.DefaultTiketiinRepository
import com.example.tiketiin.data.repository.NotificationRepository
import com.example.tiketiin.data.repository.ProfileRepository
import com.example.tiketiin.data.repository.PurchaseHistoryRepository
import com.example.tiketiin.data.repository.RepositoryProvider
import com.example.tiketiin.data.repository.SavedEventsRepository
import com.example.tiketiin.data.repository.WishlistRepository
import com.example.tiketiin.data.source.DummyDataSource
import com.example.tiketiin.domain.EventLocationResolver
import com.example.tiketiin.ui.auth.LoginScreen
import com.example.tiketiin.ui.auth.LoginViewModel
import com.example.tiketiin.ui.auth.OtpScreen
import com.example.tiketiin.ui.auth.RegisterScreen
import com.example.tiketiin.ui.auth.RegisterViewModel
import com.example.tiketiin.ui.auth.WelcomeScreen
import com.example.tiketiin.ui.checkout.CheckoutViewModel
import com.example.tiketiin.ui.checkout.OrderFormScreen
import com.example.tiketiin.ui.checkout.PaymentInstructionScreen
import com.example.tiketiin.ui.checkout.PaymentScreen
import com.example.tiketiin.ui.checkout.PaymentSuccessScreen
import com.example.tiketiin.ui.checkout.PaymentSuccessViewModel
import com.example.tiketiin.ui.components.BottomNavBar
import com.example.tiketiin.ui.detail.DetailScreen
import com.example.tiketiin.ui.detail.DetailViewModel
import com.example.tiketiin.ui.foryou.ForYouScreen
import com.example.tiketiin.ui.foryou.ForYouViewModel
import com.example.tiketiin.ui.home.FlashTicketsScreen
import com.example.tiketiin.ui.home.HomeScreen
import com.example.tiketiin.ui.home.HomeViewModel
import com.example.tiketiin.ui.notification.NotificationBadgeViewModel
import com.example.tiketiin.ui.notification.NotificationScreen
import com.example.tiketiin.ui.notification.NotificationViewModel
import com.example.tiketiin.ui.offers.TicketOffersScreen
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

@Composable
fun TiketiinApp() {
    val navController = rememberNavController()

    val authRepository = remember {
        AuthRepository()
    }

    val scope = rememberCoroutineScope()

    val context = LocalContext.current.applicationContext

    val database = remember(context) {
        DatabaseProvider.getDatabase(context)
    }

    val hotelLocalDataSource = remember(database) {
        HotelLocalDataSource(database.hotelDao())
    }

    val orderLocalDataSource = remember(database) {
        OrderLocalDataSource(database.orderDao())
    }

    // Sumber data sementara masih dummy. Nanti cukup ganti di baris ini.
    val repository = remember(
        database,
        hotelLocalDataSource,
        orderLocalDataSource
    ) {
        DefaultTiketiinRepository(
            source = DummyDataSource,
            listingDao = database.listingDao(),
            hotelLocalDataSource = hotelLocalDataSource,
            orderLocalDataSource = orderLocalDataSource
        )
    }

    val wishlistRepository = remember { WishlistRepository() }

    val eventRepository = remember(context) {
        RepositoryProvider.getEventRepository(context)
    }

    val checkoutViewModel: CheckoutViewModel = viewModel(
        factory = simpleFactory {
            CheckoutViewModel(repository)
        }
    )

    val sellViewModel: SellViewModel = viewModel(
        factory = simpleFactory {
            SellViewModel(repository)
        }
    )

    val ordersViewModel: OrdersViewModel = viewModel(
        factory = simpleFactory {
            OrdersViewModel(repository)
        }
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val registeredPhone = remember {
        mutableStateOf("")
    }

    val startDestination = remember {
        if (SessionManager.hasSession) {
            Routes.HOME
        } else {
            Routes.WELCOME
        }
    }

    fun openTab(route: String) {
        navController.navigate(route) {
            popUpTo(Routes.HOME) {
                inclusive = false
            }
            launchSingleTop = true
            restoreState = false
        }
    }

    fun enterApp() {
        check(SessionManager.hasSession) {
            "Login Supabase diperlukan."
        }

        navController.navigate(Routes.HOME) {
            popUpTo(navController.graph.id) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    fun openEvent(eventId: Int, remote: Boolean = false) {
        navController.navigate(Routes.detail(eventId, remote))
    }

    // Mulai checkout dari Detail Event atau Event Tersimpan.
    fun startCheckout(
        eventId: Int,
        listingId: Int,
        quantity: Int
    ) {
        checkoutViewModel.start(
            eventId,
            listingId,
            quantity
        )

        navController.navigate(Routes.ORDER_FORM)
    }

    fun startCheckoutFromSaved(eventId: Int) {
        scope.launch {
            val listing = repository.getCheapestListing(eventId)

            if (listing != null) {
                startCheckout(
                    eventId = eventId,
                    listingId = listing.id,
                    quantity = 1
                )
            } else {
                Toast.makeText(
                    context,
                    "Tiket tidak tersedia",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun startSelling(eventId: Int) {
        sellViewModel.startSelling(eventId) { verified ->
            navController.navigate(
                if (verified) {
                    Routes.SELL_FORM
                } else {
                    Routes.EKYC
                }
            )
        }
    }

    Scaffold(
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (currentRoute in Routes.TABS) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onSelect = { route ->
                        openTab(route)
                    }
                )
            }
        },

        ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(
                bottom = innerPadding.calculateBottomPadding()
            )
        ) {
            composable(Routes.WELCOME) {
                WelcomeScreen(
                    onRegister = {
                        navController.navigate(Routes.REGISTER)
                    },
                    onLogin = {
                        navController.navigate(Routes.LOGIN)
                    }
                )
            }

            composable(Routes.LOGIN) {
                val loginViewModel: LoginViewModel = viewModel(
                    factory = simpleFactory {
                        LoginViewModel(authRepository)
                    }
                )

                val loginState by loginViewModel.uiState
                    .collectAsStateWithLifecycle()

                LaunchedEffect(loginState.isSuccess) {
                    if (loginState.isSuccess) {
                        enterApp()
                    }
                }

                LoginScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onLogin = { email, password ->
                        loginViewModel.login(
                            email,
                            password
                        )
                    },
                    onRegister = {
                        navController.navigate(Routes.REGISTER)
                    },
                    isLoading = loginState.isLoading,
                    loginError = loginState.errorMessage
                )
            }

            composable(Routes.REGISTER) {
                val registerViewModel: RegisterViewModel = viewModel(
                    factory = simpleFactory {
                        RegisterViewModel(authRepository)
                    }
                )

                val registerState by registerViewModel.uiState
                    .collectAsStateWithLifecycle()

                LaunchedEffect(registerState.isSuccess) {
                    if (registerState.isSuccess) {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.REGISTER) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                }

                RegisterScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onRegister = {
                            name,
                            phone,
                            email,
                            password,
                            referralCode ->

                        registerViewModel.register(
                            name = name,
                            phone = phone,
                            email = email,
                            password = password,
                            referralCode = referralCode
                        )
                    },
                    onLogin = {
                        navController.navigate(Routes.LOGIN)
                    },
                    isLoading = registerState.isLoading,
                    registerError = registerState.errorMessage
                )
            }

            composable(Routes.OTP) {
                OtpScreen(
                    phone = registeredPhone.value,
                    onBack = {
                        navController.popBackStack()
                    },
                    onVerified = {
                        Toast.makeText(
                            context,
                            "Verifikasi OTP belum terhubung ke Supabase.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }

            composable(Routes.NOTIFICATIONS) {
                val notificationViewModel: NotificationViewModel =
                    viewModel(
                        factory = simpleFactory {
                            NotificationViewModel(
                                repository = NotificationRepository()
                            )
                        }
                    )

                val notificationState by notificationViewModel.uiState
                    .collectAsStateWithLifecycle()

                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                    notificationViewModel.load()
                }

                NotificationScreen(
                    uiState = notificationState,
                    onBack = {
                        navController.popBackStack()
                    },
                    onRetry = notificationViewModel::load,
                    onOpenEvent = { eventId ->
                        openEvent(eventId)
                    },
                    onMarkAsRead = notificationViewModel::markAsRead
                )
            }

            composable(Routes.HOME) {
                val notificationBadgeViewModel: NotificationBadgeViewModel =
                    viewModel(
                        factory = simpleFactory {
                            NotificationBadgeViewModel(
                                repository = NotificationRepository()
                            )
                        }
                    )

                val notificationBadgeState by notificationBadgeViewModel.uiState
                    .collectAsStateWithLifecycle()

                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                    notificationBadgeViewModel.refresh()
                }

                val viewModel: HomeViewModel = viewModel(
                    factory = simpleFactory {
                        HomeViewModel(eventRepository, wishlistRepository)
                    }
                )

                val uiState by viewModel.uiState
                    .collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    viewModel.load()
                }

                LaunchedEffect(currentRoute) {
                    if (currentRoute == Routes.HOME) {
                        viewModel.loadWishlist()
                    }
                }

                HomeScreen(
                    uiState = uiState,
                    onSearch = { category ->
                        navController.navigate(Routes.search(category))
                    },
                    onOpenFlashTickets = {
                        navController.navigate(Routes.FLASH_TICKETS)
                    },
                    onOpenSaved = {
                        navController.navigate(Routes.SAVED)
                    },
                    onOpenNotifications = {
                        navController.navigate(Routes.NOTIFICATIONS) {
                            launchSingleTop = true
                        }
                    },
                    unreadNotificationCount = notificationBadgeState.unreadCount,
                    onOpenEvent = { eventId ->
                        openEvent(eventId, remote = true)
                    },
                    onSelectCategory = viewModel::selectCategory,
                    onRetry = viewModel::load,
                    onToggleSaved = viewModel::toggleSaved,
                    onRetryWishlist = viewModel::loadWishlist
                )
            }

            composable(Routes.FOR_YOU) {
                val viewModel: ForYouViewModel = viewModel(
                    factory = simpleFactory {
                        ForYouViewModel(
                            eventRepository = eventRepository,
                            profileRepository = ProfileRepository(),
                            wishlistRepository = WishlistRepository(),
                            purchaseHistoryRepository =
                                PurchaseHistoryRepository(),
                            eventLocationResolver =
                                EventLocationResolver(
                                    context.applicationContext
                                )
                        )
                    }
                )

                val uiState by viewModel.uiState
                    .collectAsStateWithLifecycle()

                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                    viewModel.load()
                }

                ForYouScreen(
                    uiState = uiState,
                    onSelectTab = viewModel::selectTab,
                    onSelectCategory = viewModel::selectCategory,
                    onSearch = {
                        navController.navigate(Routes.search())
                    },
                    onOpenSaved = {
                        navController.navigate(Routes.SAVED)
                    },
                    onOpenEvent = { eventId ->
                        openEvent(eventId)
                    },
                    onRetry = viewModel::load
                )
            }

            // ------------------------- FLASH TIKET -------------------------

            composable(Routes.FLASH_TICKETS) {
                val viewModel: HomeViewModel = viewModel(
                    factory = simpleFactory {
                        HomeViewModel(eventRepository, wishlistRepository)
                    }
                )

                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    viewModel.load()
                }

                FlashTicketsScreen(
                    uiState = uiState,
                    onBack = {
                        navController.popBackStack()
                    },
                    onOpenEvent = { eventId ->
                        openEvent(eventId, remote = true)
                    },
                    onRetry = viewModel::load
                )
            }

            // ------------------------- TAB: JUAL TIKET (Fungsi 2) -------------------------

            composable(Routes.SELL) {
                val viewModel: HotOpportunityViewModel = viewModel(
                    factory = simpleFactory {
                        HotOpportunityViewModel(repository)
                    }
                )

                val uiState by viewModel.uiState
                    .collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    viewModel.load()
                }

                SellScreen(
                    uiState = uiState,
                    onSeeAll = {
                        navController.navigate(Routes.HOT_LIST)
                    },
                    onSellTicket = { eventId ->
                        startSelling(eventId)
                    },
                    onOpenEvent = { eventId ->
                        openEvent(eventId)
                    },
                    onRetry = viewModel::load,
                    onManageEvents = {
                        navController.navigate(Routes.MANAGE_EVENTS)
                    },
                    onMyListings = {
                        navController.navigate(Routes.MY_LISTINGS)
                    },
                    onSalesReport = {
                        navController.navigate(Routes.SALES_REPORT)
                    }
                )
            }

            composable(Routes.HOT_LIST) {
                val viewModel: HotOpportunityViewModel = viewModel(
                    factory = simpleFactory {
                        HotOpportunityViewModel(repository)
                    }
                )

                val uiState by viewModel.uiState
                    .collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    viewModel.load()
                }

                HotOpportunityListScreen(
                    uiState = uiState,
                    onBack = {
                        navController.popBackStack()
                    },
                    onToggleFilter = viewModel::toggleFilter,
                    onSellTicket = { eventId ->
                        startSelling(eventId)
                    },
                    onOpenEvent = { eventId ->
                        openEvent(eventId)
                    },
                    onRetry = viewModel::load
                )
            }

            composable("ticket_offers") {
                TicketOffersScreen(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Routes.ORDERS) {
                val uiState by ordersViewModel.uiState
                    .collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    ordersViewModel.load()
                }

                OrdersScreen(
                    uiState = uiState,
                    onSelectTab = ordersViewModel::selectTab,
                    onSelectStatus = ordersViewModel::selectStatus,
                    onRate = ordersViewModel::rate,
                    onConfirmTicket = { orderId ->
                        ordersViewModel.confirmTicket(orderId)
                    },
                    onOpenTicket = { orderId ->
                        navController.navigate(
                            Routes.ticket(orderId)
                        )
                    },
                    onRefund = { orderId ->
                        ordersViewModel.refundOrder(orderId)
                    },
                    isRefunding = uiState.isRefunding,
                    onOpenOffers = {
                        navController.navigate("ticket_offers") {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(
                route = Routes.TICKET,
                arguments = listOf(
                    navArgument("orderId") {
                        type = NavType.IntType
                    }
                )
            ) { entry ->
                val orderId = entry.arguments
                    ?.getInt("orderId")
                    ?: -1

                val viewModel: TicketViewModel = viewModel(
                    factory = simpleFactory {
                        TicketViewModel(repository)
                    }
                )

                val uiState by viewModel.uiState
                    .collectAsStateWithLifecycle()

                LaunchedEffect(orderId) {
                    viewModel.load(orderId)
                }

                TicketDetailScreen(
                    uiState = uiState,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Routes.PROFILE) {
                val viewModel: ProfileViewModel = viewModel(
                    factory = simpleFactory {
                        ProfileViewModel(
                            authRepository = authRepository,
                            profileRepository = ProfileRepository()
                        )
                    }
                )

                val uiState by viewModel.uiState
                    .collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    viewModel.load()
                }

                ProfileScreen(
                    uiState = uiState,
                    onOpenSaved = {
                        navController.navigate(Routes.SAVED)
                    },
                    onLogout = {
                        viewModel.logout {
                            navController.navigate(Routes.WELCOME) {
                                popUpTo(navController.graph.id) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    },
                    onRetry = {
                        viewModel.load()
                    },
                    onSaveProfile = { name, city ->
                        viewModel.updateProfile(
                            name = name,
                            city = city
                        )
                    },
                    onResetSaveStatus = {
                        viewModel.resetSaveStatus()
                    },
                    onLoadCityOptions = {
                        viewModel.loadCityOptions()
                    },
                    onRetryLoadCityOptions = {
                        viewModel.retryLoadCityOptions()
                    },
                    onChangePassword = {
                            currentPassword,
                            newPassword ->

                        viewModel.changePassword(
                            currentPassword = currentPassword,
                            newPassword = newPassword
                        )
                    },
                    onResetPasswordStatus = {
                        viewModel.resetPasswordStatus()
                    },
                    onUpdateRecommendationNotifications = { enabled ->
                        viewModel.updateRecommendationNotifications(
                            enabled
                        )
                    },
                    onResetNotificationPreferenceError = {
                        viewModel.resetNotificationPreferenceError()
                    }
                )
            }

            composable(
                route = Routes.SEARCH,
                arguments = listOf(
                    navArgument("category") {
                        type = NavType.StringType
                    }
                )
            ) { entry ->
                val categoryName = entry.arguments
                    ?.getString("category")
                    ?: "all"

                val initialCategory = EventCategory.entries.find {
                    it.name == categoryName
                }

                val viewModel: SearchViewModel = viewModel(
                    factory = simpleFactory {
                        SearchViewModel(
                            eventRepository = eventRepository,
                            wishlistRepository = wishlistRepository,
                            initialCategory = initialCategory,
                            initialMode = categoryName
                        )
                    }
                )

                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                SearchScreen(
                    uiState = uiState,
                    onBack = {
                        navController.popBackStack()
                    },
                    onQueryChange = viewModel::updateQuery,
                    onSelectCategory = viewModel::selectCategory,
                    onToggleSort = viewModel::toggleSort,
                    onToggleSaved = viewModel::toggleSaved,
                    onOpenEvent = { eventId ->
                        openEvent(eventId, remote = true)
                    },
                    onRetry = viewModel::refresh
                )
            }

            composable(Routes.SAVED) {
                val viewModel: SavedViewModel = viewModel(
                    factory = simpleFactory {
                        SavedViewModel(
                            SavedEventsRepository(
                                wishlistRepository,
                                eventRepository
                            )
                        )
                    }
                )

                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    viewModel.load()
                }

                SavedScreen(
                    uiState = uiState,
                    onBack = {
                        navController.popBackStack()
                    },
                    onSelectCategory = viewModel::selectCategory,
                    onToggleSelected = viewModel::toggleSelected,
                    onRemove = viewModel::remove,
                    onOpenEvent = { eventId ->
                        openEvent(eventId, remote = true)
                    },
                    onCheckout = { eventId ->
                        startCheckoutFromSaved(eventId)
                    },
                    onRetry = viewModel::load
                )
            }

            composable(
                route = Routes.DETAIL,
                arguments = listOf(
                    navArgument("eventId") {
                        type = NavType.IntType
                    },
                    navArgument("remote") {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) { entry ->
                val eventId = entry.arguments?.getInt("eventId") ?: -1

                val viewModel: DetailViewModel = viewModel(
                    factory = simpleFactory {
                        DetailViewModel(
                            eventRepository = eventRepository,
                            wishlistRepository = wishlistRepository,
                            repository = repository
                        )
                    }
                )

                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(eventId) {
                    viewModel.load(eventId)
                }

                DetailScreen(
                    uiState = uiState,
                    onBack = {
                        navController.popBackStack()
                    },
                    onSelectTab = viewModel::selectTab,
                    onChangeQuantity = viewModel::changeQuantity,
                    onSelectListing = viewModel::selectListing,
                    onToggleSaved = viewModel::toggleSaved,
                    onSubmitOffer = viewModel::submitOffer,
                    onResetOfferResult = viewModel::resetOfferResult,
                    onViewOffers = {
                        navController.navigate("ticket_offers") {
                            launchSingleTop = true
                        }
                    },
                    onBuy = { id, listingId, quantity ->
                        startCheckout(id, listingId, quantity)
                    }
                )
            }

            composable(Routes.ORDER_FORM) {
                val uiState by checkoutViewModel.uiState
                    .collectAsStateWithLifecycle()

                OrderFormScreen(
                    uiState = uiState,
                    onBack = {
                        navController.popBackStack()
                    },
                    onNameChange = checkoutViewModel::updateName,
                    onEmailChange = checkoutViewModel::updateEmail,
                    onPhoneChange = checkoutViewModel::updatePhone,
                    onIdNumberChange = checkoutViewModel::updateIdNumber,
                    onAddressChange = checkoutViewModel::updateAddress,
                    onTogglePowerOfAttorney =
                        checkoutViewModel::togglePowerOfAttorney,
                    onContinue = {
                        if (checkoutViewModel.validateForm()) {
                            navController.navigate(Routes.PAYMENT)
                        }
                    }
                )
            }

            composable(Routes.PAYMENT) {
                val uiState by checkoutViewModel.uiState
                    .collectAsStateWithLifecycle()

                PaymentScreen(
                    uiState = uiState,
                    onBack = {
                        navController.popBackStack()
                    },
                    onSelectCategory = checkoutViewModel::selectPaymentCategory,
                    onSelectMethod = checkoutViewModel::selectPaymentMethod,
                    onPay = {
                        checkoutViewModel.preparePayment { orderId ->
                            navController.navigate(
                                Routes.PAYMENT_INSTRUCTION
                            )
                        }
                    }
                )
            }

            composable(Routes.PAYMENT_INSTRUCTION) {
                val uiState by checkoutViewModel.uiState
                    .collectAsStateWithLifecycle()

                val orderId = uiState.pendingOrderId ?: -1

                PaymentInstructionScreen(
                    uiState = uiState,
                    orderId = orderId,
                    onBack = {
                        navController.popBackStack()
                    },
                    onConfirmPayment = {
                        checkoutViewModel.confirmPayment { paidOrderId ->
                            navController.navigate(
                                Routes.paymentSuccess(paidOrderId)
                            ) {
                                popUpTo(Routes.HOME)
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }

            composable(
                route = Routes.PAYMENT_SUCCESS,
                arguments = listOf(
                    navArgument("orderId") {
                        type = NavType.IntType
                    }
                )
            ) { entry ->

                val orderId = entry.arguments
                    ?.getInt("orderId")
                    ?: -1

                val viewModel: PaymentSuccessViewModel = viewModel(
                    factory = simpleFactory {
                        PaymentSuccessViewModel(
                            repository = repository,
                            eventCatalogRepository = eventRepository
                        )
                    }
                )

                val uiState by viewModel.uiState
                    .collectAsStateWithLifecycle()

                LaunchedEffect(orderId) {
                    viewModel.load(orderId)
                }

                val goToOrders = {
                    ordersViewModel.selectTab(0)
                    ordersViewModel.selectStatus(
                        OrderStatus.DIPROSES
                    )
                    openTab(Routes.ORDERS)
                }

                PaymentSuccessScreen(
                    uiState = uiState,
                    onBack = {
                        goToOrders()
                    },
                    onViewOrders = {
                        goToOrders()
                    }
                )
            }

            composable(Routes.EKYC) {
                val uiState by sellViewModel.uiState
                    .collectAsStateWithLifecycle()

                EkycScreen(
                    uiState = uiState,
                    onBack = {
                        navController.popBackStack()
                    },
                    onToggleKtp = sellViewModel::toggleKtp,
                    onToggleSelfie = sellViewModel::toggleSelfie,
                    onNameChange = sellViewModel::updateKycName,
                    onNikChange = sellViewModel::updateKycNik,
                    onSubmit = {
                        sellViewModel.submitKyc {
                            navController.navigate(
                                Routes.SELL_FORM
                            ) {
                                popUpTo(Routes.EKYC) {
                                    inclusive = true
                                }
                            }
                        }
                    }
                )
            }

            composable(Routes.SELL_FORM) {
                val uiState by sellViewModel.uiState
                    .collectAsStateWithLifecycle()

                SellFormScreen(
                    uiState = uiState,
                    onBack = {
                        navController.popBackStack()
                    },
                    onSelectEvent = sellViewModel::selectEvent,
                    onSeatCategoryChange =
                        sellViewModel::updateSeatCategory,
                    onRowChange = sellViewModel::updateRow,
                    onQuantityChange = sellViewModel::updateQuantity,
                    onFaceValueChange =
                        sellViewModel::updateFaceValue,
                    onPriceChange = sellViewModel::updatePrice,
                    onToggleProof = sellViewModel::toggleProof,
                    onPickProof = { bytes, mime, name -> sellViewModel.uploadProof(bytes, mime, name) },
                    onSubmit = {
                        sellViewModel.submitForm {
                            navController.navigate(
                                Routes.PRICE_CHECK
                            )
                        }
                    }
                )
            }

            composable(Routes.PRICE_CHECK) {
                val uiState by sellViewModel.uiState
                    .collectAsStateWithLifecycle()

                PriceCheckScreen(
                    uiState = uiState,
                    onBack = {
                        navController.popBackStack()
                    },
                    onPublish = { price ->
                        sellViewModel.publish(price) {
                            navController.navigate(
                                Routes.LISTING_PUBLISHED
                            ) {
                                popUpTo(Routes.SELL)
                            }
                        }
                    }
                )
            }

            composable(Routes.LISTING_PUBLISHED) {
                val uiState by sellViewModel.uiState
                    .collectAsStateWithLifecycle()

                ListingPublishedScreen(
                    uiState = uiState,
                    onViewStoreOrders = {
                        ordersViewModel.selectTab(1)
                        ordersViewModel.selectStatus(
                            OrderStatus.DIPROSES
                        )
                        openTab(Routes.ORDERS)
                    },
                    onViewListings = {
                        navController.navigate(
                            Routes.MY_LISTINGS
                        ) {
                            popUpTo(Routes.SELL)
                        }
                    }
                )
            }

            composable(Routes.MANAGE_EVENTS) {
                val viewModel: ManageEventsViewModel = viewModel(
                    factory = simpleFactory {
                        ManageEventsViewModel(repository)
                    }
                )

                val uiState by viewModel.uiState
                    .collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    viewModel.load()
                }

                ManageEventsScreen(
                    uiState = uiState,
                    onBack = {
                        navController.popBackStack()
                    },
                    formatDate = viewModel::formatDate,
                    onSave = viewModel::save,
                    onDelete = viewModel::delete,
                    onClearError = viewModel::clearError
                )
            }

            composable(Routes.MY_LISTINGS) {
                val viewModel: MyListingsViewModel = viewModel(
                    factory = simpleFactory {
                        MyListingsViewModel(repository)
                    }
                )

                val uiState by viewModel.uiState
                    .collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    viewModel.load()
                }

                MyListingsScreen(
                    uiState = uiState,
                    onBack = {
                        navController.popBackStack()
                    },
                    onUpdatePrice = viewModel::updatePrice,
                    onDelete = viewModel::delete,
                    onClearError = viewModel::clearError,
                    onAddListing = {
                        startSelling(-1)
                    }
                )
            }

            composable(Routes.SALES_REPORT) {
                val viewModel: SalesViewModel = viewModel(
                    factory = simpleFactory {
                        SalesViewModel(repository)
                    }
                )

                val uiState by viewModel.uiState
                    .collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    viewModel.load()
                }

                SalesReportScreen(
                    uiState = uiState,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}