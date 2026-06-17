package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.R
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.ClickAndGoViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClickAndGoAppContent(viewModel: ClickAndGoViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val language by viewModel.language.collectAsState()
    val activeRole by viewModel.userRoleState.collectAsState()

    MyApplicationTheme(darkTheme = true) { // Force Dark Premium mode for sleek Glassmorphic Tech theme
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF04181B), // Deep Radiant Emerald Dark Cyan
                            Color(0xFF0A231C), // Glowing Forest Mint Green
                            Color(0xFF060D14)  // Sleek Carbon Slate Black
                        )
                    )
                )
        ) {
            if (!isLoggedIn) {
                LoginOrRegisterScreen(viewModel)
            } else {
                MainAppLayout(viewModel)
            }

            // Always accessible, floating premium Sandbox Console Anchor on bottom right!
            SandboxFloatingConsole(viewModel)
        }
    }
}

@Composable
fun LoginOrRegisterScreen(viewModel: ClickAndGoViewModel) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var phoneInput by remember { mutableStateOf("01712345678") }
    var pinInput by remember { mutableStateOf("1234") }
    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var roleInput by remember { mutableStateOf("CONSUMER") } // CONSUMER or MERCHANT
    var pinVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firebaseConnected by viewModel.firebaseConnected.collectAsState()
    val fcmToken by viewModel.firebaseTokenId.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Identity Header Card
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(DarkPrimary, DarkSecondary)
                    )
                )
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = "Logo",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Click 'n Go",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 0.5.sp
            )
        )

        Text(
            text = "Bangladesh's Premier Dynamic Yield Super App",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.LightGray.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
        )

        // Firebase Connectivity Live Sync status
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .padding(vertical = 12.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Color(0xFF0F2D24))
                .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(30.dp))
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981))
            )
            Text(
                text = "Firebase Connected & Synced",
                color = Color(0xFF34D399),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Glassmorphic interactive Login / Sign Up Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = GlassBg),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0x33FFFFFF))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Segmented tab selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF132030))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isRegisterMode) DarkPrimary else Color.Transparent)
                            .clickable { 
                                isRegisterMode = false
                                errorMessage = ""
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Login Account", 
                            color = if (!isRegisterMode) Color.Black else Color.Gray, 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 13.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isRegisterMode) DarkPrimary else Color.Transparent)
                            .clickable { 
                                isRegisterMode = true
                                errorMessage = ""
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Register Wallet", 
                            color = if (isRegisterMode) Color.Black else Color.Gray, 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 13.sp
                        )
                    }
                }

                Text(
                    text = if (isRegisterMode) "Create Free High-Yield Wallet" else "Secure Access & Yield Verification",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (isRegisterMode) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Full Name", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = DarkPrimary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkPrimary,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email Address", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = DarkPrimary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkPrimary,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    label = { Text("Mobile Number", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = DarkPrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkPrimary,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { if (it.length <= 4) pinInput = it },
                    label = { Text("4-Digit Security PIN", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = DarkPrimary) },
                    trailingIcon = {
                        IconButton(onClick = { pinVisible = !pinVisible }) {
                            Icon(
                                imageVector = if (pinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle PIN visibility",
                                tint = Color.Gray
                            )
                        }
                    },
                    visualTransformation = if (pinVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkPrimary,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (isRegisterMode) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Interactive Live Role Selection:",
                        color = Color.LightGray,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // CONSUMER selection card
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (roleInput == "CONSUMER") Color(0x3310B981) else Color(0xFF132030))
                                .border(1.dp, if (roleInput == "CONSUMER") DarkPrimary else Color.Transparent, RoundedCornerShape(12.dp))
                                .clickable { roleInput = "CONSUMER" }
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = if (roleInput == "CONSUMER") DarkPrimary else Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Consumer", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                        // MERCHANT selection card
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (roleInput == "MERCHANT") Color(0x3310B981) else Color(0xFF132030))
                                .border(1.dp, if (roleInput == "MERCHANT") DarkPrimary else Color.Transparent, RoundedCornerShape(12.dp))
                                .clickable { roleInput = "MERCHANT" }
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Store, contentDescription = null, tint = if (roleInput == "MERCHANT") DarkPrimary else Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Merchant Store", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(errorMessage, color = Color(0xFFF87171), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (isRegisterMode) {
                            if (nameInput.isEmpty() || emailInput.isEmpty() || phoneInput.isEmpty() || pinInput.length < 4) {
                                errorMessage = "Please fill in all details! PIN must be 4 digits."
                            } else {
                                errorMessage = ""
                                // Register mock insert
                                val newUserObj = UserEntity(
                                    id = phoneInput,
                                    name = nameInput,
                                    email = emailInput,
                                    pin = pinInput,
                                    kycStatus = "NOT_STARTED",
                                    role = roleInput,
                                    balanceBdt = 500.0, // Welcome signup promo starter balance!
                                    balanceUsd = 0.0,
                                    loyaltyPoints = 50,
                                    referralsCount = 0
                                )
                                scope.launch {
                                    viewModel.dao.insertUser(newUserObj)
                                    if (roleInput == "MERCHANT") {
                                        viewModel.dao.insertMerchant(MerchantEntity(
                                            merchantId = phoneInput,
                                            businessName = "$nameInput Store",
                                            category = "Retail",
                                            dailySalesBdt = 0.0,
                                            monthlySalesBdt = 0.0,
                                            settlementRequestedBdt = 0.0,
                                            staticQrCodeData = "clickngo://pay?merchantId=$phoneInput&merchantName=${nameInput.replace(" ", "%20")}"
                                        ))
                                    }
                                    viewModel.currentUserId.value = phoneInput
                                    viewModel.userRoleState.value = roleInput
                                    viewModel.isLoggedIn.value = true
                                }
                            }
                        } else {
                            viewModel.currentUserId.value = phoneInput
                            if (viewModel.login(pinInput)) {
                                errorMessage = ""
                                // Auto sync role
                                val userObj = viewModel.users.value.find { it.id == phoneInput }
                                if (userObj != null) {
                                    viewModel.userRoleState.value = userObj.role
                                }
                            } else {
                                errorMessage = "Incorrect PIN or Mobile number. Select quick accounts below."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkPrimary,
                        contentColor = Color(0xFF09121A)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isRegisterMode) "Create Wallet & Claim ৳500" else "Authorize Secure Verification PIN",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Demo Accounts Loader
                Text(
                    text = "Tap to Auto-Fill Live Demo Credentials:",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E2E3E))
                            .clickable {
                                phoneInput = "01712345678"
                                pinInput = "1234"
                                isRegisterMode = false
                            }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("User", color = DarkPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E2E3E))
                            .clickable {
                                phoneInput = "01812345678"
                                pinInput = "1122"
                                isRegisterMode = false
                            }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Merchant", color = Color(0xFFA7F3D0), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E2E3E))
                            .clickable {
                                phoneInput = "01912345678"
                                pinInput = "9999"
                                isRegisterMode = false
                            }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Admin", color = Color(0xFFFCA5A5), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Direct bypass biometrics simulated click target
        IconButton(
            onClick = {
                viewModel.bypassLogin()
            },
            modifier = Modifier
                .clip(CircleShape)
                .background(Color(0x333388FF))
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = "Simulated Biometrics bypass",
                tint = DarkSecondary,
                modifier = Modifier.size(36.dp)
            )
        }
        Text(
            text = "Simulated Biometrics Access Check",
            color = Color.LightGray,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppLayout(viewModel: ClickAndGoViewModel) {
    val activeRole by viewModel.userRoleState.collectAsState()
    val activeUser by viewModel.currentUser.collectAsState(initial = null)
    val appLanguage by viewModel.language.collectAsState()

    var showNotificationsSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(colors = listOf(DarkPrimary, DarkSecondary))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Click 'n Go",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                            )
                            Text(
                                text = "Bangladesh Super App",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray)
                            )
                        }
                    }
                },
                actions = {
                    // Language Switcher
                    TextButton(onClick = { viewModel.toggleLanguage() }) {
                        Text(
                            text = if (appLanguage == "EN") "বাংলা" else "English",
                            color = DarkPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Notification Button
                    IconButton(onClick = { showNotificationsSheet = true }) {
                        Box {
                            Icon(Icons.Default.Notifications, contentDescription = "Alerts", tint = Color.White)
                            val notifications by viewModel.currentNotifications.collectAsState()
                            val unreadCount = notifications.filter { !it.isRead }.size
                            if (unreadCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .align(Alignment.TopEnd)
                                        .background(FraudFlagColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        unreadCount.toString(),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Logout Button
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Log out", tint = Color.LightGray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF06101E))
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeRole) {
                "CONSUMER" -> ConsumerDashboard(viewModel)
                "MERCHANT" -> MerchantDashboardLayout(viewModel)
                "ADMIN" -> AdminDashboardLayout(viewModel)
                "TECH_DOCS" -> TechDocsPortalScreen(viewModel)
            }

            if (showNotificationsSheet) {
                NotificationOverlay(viewModel, onClose = { showNotificationsSheet = false })
            }
        }
    }
}

@Composable
fun ConsumerDashboard(viewModel: ClickAndGoViewModel) {
    val user by viewModel.currentUser.collectAsState(initial = null)
    val transactions by viewModel.currentTransactions.collectAsState(initial = emptyList())

    var currentTab by remember { mutableStateOf("HOME") }
    // Active Bottom Actions sheet triggers
    var activeBottomSheet by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .background(Color(0xF609121A)) // High contrast Deep Space dark color
                    .border(1.dp, Color(0x1BFFFFFF), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .padding(vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Home Tab
                    Column(
                        modifier = Modifier
                            .clickable { currentTab = "HOME" }
                            .padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            tint = if (currentTab == "HOME") DarkPrimary else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            "Home",
                            color = if (currentTab == "HOME") DarkPrimary else Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == "HOME") FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    // Transfer Tab
                    Column(
                        modifier = Modifier
                            .clickable { currentTab = "TRANSFER" }
                            .padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Transfer",
                            tint = if (currentTab == "TRANSFER") DarkPrimary else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            "Transfer",
                            color = if (currentTab == "TRANSFER") DarkPrimary else Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == "TRANSFER") FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    // Activity Tab
                    Column(
                        modifier = Modifier
                            .clickable { currentTab = "ACTIVITY" }
                            .padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Activity",
                            tint = if (currentTab == "ACTIVITY") DarkPrimary else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            "Activity",
                            color = if (currentTab == "ACTIVITY") DarkPrimary else Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == "ACTIVITY") FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    // Profile Tab
                    Column(
                        modifier = Modifier
                            .clickable { currentTab = "PROFILE" }
                            .padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = if (currentTab == "PROFILE") DarkPrimary else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            "Profile",
                            color = if (currentTab == "PROFILE") DarkPrimary else Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == "PROFILE") FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                "HOME" -> HomeTabContent(
                    user = user,
                    viewModel = viewModel,
                    onAddMoneyClick = { activeBottomSheet = "ADD_MONEY" },
                    onRewardsClick = { activeBottomSheet = "REWARDS" },
                    onKycClick = { activeBottomSheet = "EKYC" },
                    onConvertClick = { activeBottomSheet = "CONVERT" },
                    onLoanRequestClick = { activeBottomSheet = "LOAN_REQUEST" },
                    onMobileRechargeClick = { activeBottomSheet = "MOBILE_RECHARGE" }
                )
                "TRANSFER" -> TransferTabContent(
                    user = user,
                    viewModel = viewModel,
                    onSendTrigger = { activeBottomSheet = "SEND_MONEY" }
                )
                "ACTIVITY" -> ActivityTabContent(
                    transactions = transactions,
                    viewModel = viewModel
                )
                "PROFILE" -> ProfileTabContent(
                    user = user,
                    viewModel = viewModel,
                    onVerifyClick = { activeBottomSheet = "EKYC" }
                )
            }
        }
    }

    // Sheet / Dialogs renderer
    activeBottomSheet?.let { sheet ->
        when (sheet) {
            "ADD_MONEY" -> AddMoneySheet(viewModel, onDismiss = { activeBottomSheet = null })
            "SEND_MONEY" -> SendMoneySheet(viewModel, onDismiss = { activeBottomSheet = null })
            "VIRTUAL_CARDS" -> VirtualCardsSheet(viewModel, onDismiss = { activeBottomSheet = null })
            "PAY_BILLS" -> PayBillsSheet(viewModel, onDismiss = { activeBottomSheet = null })
            "REWARDS" -> RewardsSheet(viewModel, onDismiss = { activeBottomSheet = null })
            "EKYC" -> EkycSheet(viewModel, onDismiss = { activeBottomSheet = null })
            "CONVERT" -> CurrencyConvertSheet(viewModel, onDismiss = { activeBottomSheet = null })
            "LOAN_REQUEST" -> LoanRequestSheet(viewModel, onDismiss = { activeBottomSheet = null })
            "MOBILE_RECHARGE" -> MobileRechargeSheet(viewModel, onDismiss = { activeBottomSheet = null })
        }
    }
}

@Composable
fun HomeTabContent(
    user: UserEntity?,
    viewModel: ClickAndGoViewModel,
    onAddMoneyClick: () -> Unit,
    onRewardsClick: () -> Unit,
    onKycClick: () -> Unit,
    onConvertClick: () -> Unit,
    onLoanRequestClick: () -> Unit,
    onMobileRechargeClick: () -> Unit
) {
    var isAssetVisible by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Sleek Search & Flag Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Bangladesh Flag Selector (Touch 'n Go MY style)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color(0xFF1E2E3E))
                    .border(1.dp, Color(0xFF019B5D), RoundedCornerShape(30.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Quick Canvas Bangladesh Flag
                    Canvas(modifier = Modifier.size(16.dp)) {
                        drawCircle(color = Color(0xFF016A4E)) // Green sphere base
                        drawCircle(color = Color(0xFFF42A41), radius = size.minDimension / 4f) // Red center
                    }
                    Text("BD", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Dropdown",
                        tint = Color.LightGray,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Search pill bar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color.White)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Text("Search Services & Deals", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }

        // 2. High-Tech Balance Asset Panel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Total asset", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Icon(
                        imageVector = if (isAssetVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "View",
                        tint = Color.LightGray,
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { isAssetVisible = !isAssetVisible }
                    )
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Secure",
                        tint = DarkPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = if (user != null && isAssetVisible) "৳ " + viewModel.decimalFormat(user.balanceBdt) else "৳ ••••••••",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // Pill add money
            Button(
                onClick = onAddMoneyClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x3302C382)),
                border = BorderStroke(1.dp, DarkPrimary),
                shape = RoundedCornerShape(30.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Add money", color = DarkPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = DarkPrimary, modifier = Modifier.size(10.dp))
                }
            }
        }

        // 3. Dual-Currency quick switcher indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0C1D30))
                .border(1.dp, Color(0x1BFFFFFF), RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CurrencyExchange, contentDescription = null, tint = DarkTertiary, modifier = Modifier.size(16.dp))
                    Column {
                        Text("Freelancer Global Ledger", color = Color.Gray, fontSize = 10.sp)
                        Text(
                            text = if (user != null && isAssetVisible) "$ " + viewModel.decimalFormat(user.balanceUsd) else "$ ••••••••",
                            color = DarkTertiary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                TextButton(
                    onClick = onConvertClick,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("Convert now", color = DarkPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 4. White Super-App Service Hub Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Request Loan
                Column(
                    modifier = Modifier
                        .clickable(onClick = onLoanRequestClick)
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFEDE5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Handshake, contentDescription = "Request Loan", tint = Color(0xFFE04F2A), modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Instant Loan", color = Color(0xFF1E293B), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Live Yield Engine Quick Hub Action
                Column(
                    modifier = Modifier
                        .clickable(onClick = onAddMoneyClick)
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD1FAE5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.TrendingUp, contentDescription = "Live Yield", tint = Color(0xFF059669), modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("0.05%/sec Yield", color = Color(0xFF047857), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // 5. Dual side promo blocks (Grow Money, GOrewards & Live Interest Ticker)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Left block (Double stacked)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Grow your money
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFEFF6FF))
                        .border(1.dp, Color(0xFFDBEAFE), RoundedCornerShape(16.dp))
                        .clickable { onAddMoneyClick() }
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Eco, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                        Column {
                            Text("Grow your money", color = Color(0xFF1E3A8A), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("Earn ৳100 welcome", color = Color(0xFF4B5563), fontSize = 9.sp)
                        }
                    }
                }

                // GOrewards loyalty points
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFFBEB))
                        .border(1.dp, Color(0xFFFEF3C7), RoundedCornerShape(16.dp))
                        .clickable { onRewardsClick() }
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                        Column {
                            Text("GOrewards", color = Color(0xFF78350F), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("${user?.loyaltyPoints ?: 0} pts active", color = Color(0xFFD97706), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }
                }
            }

            // Right tall block (Live Yield Ticker tracking session growth)
            val liveEarnings by viewModel.liveEarningsState.collectAsState()
            val userBalance = user?.balanceBdt ?: 0.0
            val activeRateLabel = when {
                userBalance < 10.0 -> "Min ৳10 needed"
                userBalance <= 1000.0 -> "৳0.0005/s flat"
                userBalance <= 5000.0 -> "0.005%/s"
                userBalance <= 20000.0 -> "৳0.05/s flat"
                else -> "0.5%/s"
            }
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF092923))
                    .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .clickable { viewModel.triggerEvent("Live Wallet Earning active! Tiered Rates: 10-1k BDT: 0.0005tk/s | 1k-5k BDT: 0.005%/s | 5k-20k BDT: 0.05tk/s | 20k+ BDT: 0.5%/s") }
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0x3310B981)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                        }
                        Column {
                            Text("Live Yield", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text(activeRateLabel, color = Color(0xFFA7F3D0), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Column {
                        Text(
                            "Session Earned:",
                            color = Color.LightGray,
                            fontSize = 9.sp
                        )
                        Text(
                            text = "৳ ${String.format("%.4f", liveEarnings)}",
                            color = Color(0xFF34D399),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                        Text(
                            text = "Ticking live...",
                            color = Color.Gray,
                            fontSize = 8.sp
                        )
                    }
                }
            }
        }

        // 6. Custom High-Yield Live Earnings Promo Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(10.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF092923)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Color(0xFF0C1D1A))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Text("ACTIVE MULTI-TIER LEDGER YIELD SPEED", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Dynamic Live Yield Ticker Active",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "৳10-1k: ৳0.0005/s | ৳1k-5k: 0.005%/s\n৳5k-20k: ৳0.05/s | Over ৳20k: 0.5% per second!",
                            color = Color(0xFF86EFAC),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 13.sp
                        )
                        Text(
                            text = "Your funds grow live in real time. Standard transfers require instant admin approval.",
                            color = Color.LightGray,
                            fontSize = 9.sp,
                            lineHeight = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // Overlay brand chips
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF059669))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Live Ticker", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                    }
                }

                // Banner Details label
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF041C17))
                        .padding(12.dp)
                        .clickable { onAddMoneyClick() },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Ready to add funds to boost yield?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Tap to deposit with bKash, Nagad, Rocket or Midland Bank", color = Color.LightGray, fontSize = 10.sp)
                    }
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(16.dp))
                }
            }
        }

        // Carousel Page Dots
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981))) // Active green dot
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.Gray))
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.Gray))
            }
        }

        // 7. Recommended Section
        Text("Recommended", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Remittance
            Column(
                modifier = Modifier
                    .clickable { viewModel.triggerEvent("Launch International Remittance gateway portal.") }
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.size(46.dp).clip(CircleShape).background(Color(0xFF1E2E3E)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = DarkSecondary)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("Remittance", color = Color.LightGray, fontSize = 11.sp, textAlign = TextAlign.Center)
            }

            // Visa Card
            Column(
                modifier = Modifier
                    .clickable { viewModel.triggerEvent("Opening Secure Visa Prepaid card dashboard.") }
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.size(46.dp).clip(CircleShape).background(Color(0xFF1E2E3E)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = DarkTertiary)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("Visa Card", color = Color.LightGray, fontSize = 11.sp, textAlign = TextAlign.Center)
            }

            // International
            Column(
                modifier = Modifier
                    .clickable { onConvertClick() }
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.size(46.dp).clip(CircleShape).background(Color(0xFF1E2E3E)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = DarkPrimary)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("International", color = Color.LightGray, fontSize = 11.sp, textAlign = TextAlign.Center)
            }

            // Global Service
            Column(
                modifier = Modifier
                    .clickable { onRewardsClick() }
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.size(46.dp).clip(CircleShape).background(Color(0xFF1E2E3E)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.LocalActivity, contentDescription = null, tint = GoldReward)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("Global service", color = Color.LightGray, fontSize = 11.sp, textAlign = TextAlign.Center)
            }
        }

        // 8. My Favourites Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("My Favourites", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                "Edit",
                color = DarkSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { viewModel.triggerEvent("Click 'n Go layout customizations unlocked (Simulated)") }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Refer
            Column(
                modifier = Modifier
                    .clickable { viewModel.triggerEvent("Loyalty Referral Link: [clickNgo.com/ref/invite] copied successfully!") }
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(Color(0xFF152A3A)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color(0xFFF97316))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("Refer & earn", color = Color.LightGray, fontSize = 11.sp, textAlign = TextAlign.Center)
            }

            // BD Prepaid
            Column(
                modifier = Modifier
                    .clickable(onClick = onMobileRechargeClick)
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(Color(0xFF152A3A)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = Color(0xFF2563EB))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("BD Prepaid", color = Color.LightGray, fontSize = 11.sp, textAlign = TextAlign.Center)
            }

            // e-Mas
            Column(
                modifier = Modifier
                    .clickable { viewModel.triggerEvent("Check Gold Investment e-Mas savings rates: 22K standard at BDT 11,200/gram.") }
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(Color(0xFF152A3A)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFFFFB000))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("e-Mas", color = Color.LightGray, fontSize = 11.sp, textAlign = TextAlign.Center)
            }
        }

        // KYC Verification card reminder if user KYC is not approved
        if (user?.kycStatus != "APPROVED") {
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x33FFC107))
                    .border(1.dp, Color(0xFFFFC107), RoundedCornerShape(14.dp))
                    .clickable { onKycClick() }
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFFC107))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("National ID unverified (PENDING eKYC)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Verify your identity with National ID today to lift limits.", color = Color.LightGray, fontSize = 11.sp)
                    }
                    Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}

@Composable
fun TransferTabContent(
    user: UserEntity?,
    viewModel: ClickAndGoViewModel,
    onSendTrigger: () -> Unit
) {
    var recipientPhone by remember { mutableStateOf("") }
    var transferAmount by remember { mutableStateOf("") }
    var pinValue by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tab Heading
        Text(
            "Peer-to-Peer Transfer Desk",
            style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
        )

        // Wallet Balance Info
        Card(
            colors = CardDefaults.cardColors(containerColor = GlassBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Sender Ledger Info", color = Color.LightGray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("BDT balance: ৳ " + (user?.balanceBdt ?: 0.0), color = Color.White, fontWeight = FontWeight.Bold)
                    Text("USD balance: $ " + (user?.balanceUsd ?: 0.0), color = DarkTertiary)
                }
            }
        }

        // Direct Inputs
        OutlinedTextField(
            value = recipientPhone,
            onValueChange = { recipientPhone = it },
            label = { Text("Recipient Phone Number (01xxxxxxxxx)", color = Color.Gray) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DarkPrimary,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Contact Book Quick selection
        Text("Quick Recipients", color = Color.Gray, fontSize = 12.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val quickContacts = listOf(
                "01787654321" to "Rahman MD",
                "01912345678" to "Ayesha"
            )
            quickContacts.forEach { contact ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(Color(0xFF1E2E3E))
                        .clickable { recipientPhone = contact.first }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(contact.second, color = DarkPrimary, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                }
            }
        }

        OutlinedTextField(
            value = transferAmount,
            onValueChange = { transferAmount = it },
            label = { Text("Amount (BDT)", color = Color.Gray) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DarkPrimary,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Quick amount buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("100", "500", "1000", "5000").forEach { valAmount ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x3302C382))
                        .border(1.dp, DarkPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable { transferAmount = valAmount }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("৳ $valAmount", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            label = { Text("Transfer Memo / Note (Optional)", color = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DarkPrimary,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = pinValue,
            onValueChange = { pinValue = it },
            label = { Text("Private PIN Verification", color = Color.Gray) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DarkPrimary,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Warning anti-fraud limit card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x16FF4D4D))
                .border(1.dp, FraudFlagColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Text(
                "Anti-Fraud Warning: Single transfers exceeding BDT 25,000 threshold limits undergo dynamic Admin Desk manual compliance reviews for super-app security.",
                color = FraudFlagColor,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (recipientPhone.isEmpty() || transferAmount.isEmpty() || pinValue.isEmpty()) {
                    viewModel.triggerEvent("Please fill phone number, amount, and secure PIN parameters.")
                    return@Button
                }
                val amt = transferAmount.toDoubleOrNull()
                if (amt == null || amt <= 0) {
                    viewModel.triggerEvent("Invalid transfer amount parameter!")
                    return@Button
                }

                val success = viewModel.sendMoney(
                    receiverPhone = recipientPhone,
                    amount = amt,
                    pin = pinValue,
                    note = noteText.ifEmpty { "P2P Payment" }
                )
                if (success) {
                    recipientPhone = ""
                    transferAmount = ""
                    pinValue = ""
                    noteText = ""
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = DarkPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Send Instant Funds Now", color = Color(0xFF09121A), fontWeight = FontWeight.Bold)
        }

        // Secondary launch option
        TextButton(
            onClick = onSendTrigger,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("More Advanced Transfer Schemes", color = DarkSecondary)
        }
    }
}

@Composable
fun ActivityTabContent(
    transactions: List<TransactionEntity>,
    viewModel: ClickAndGoViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Activity & Ledger",
                    style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
                )
                Text(
                    "Transparent Multi-Currency history",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }

            // Exporter
            TextButton(
                onClick = {
                    viewModel.triggerEvent("Statement PDF compiled! File saved in Click 'n Go secure local storage.")
                }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = DarkSecondary, modifier = Modifier.size(16.dp))
                    Text("Export PDF", color = DarkSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.History, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Your ledger is currently empty.", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(transactions) { tx ->
                    TransactionRowItem(tx, viewModel)
                }
            }
        }
    }
}

@Composable
fun ProfileTabContent(
    user: UserEntity?,
    viewModel: ClickAndGoViewModel,
    onVerifyClick: () -> Unit
) {
    var isBiometrics by remember { mutableStateOf(user?.isBiometricEnabled ?: false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "My Account Profile",
            style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
        )

        // User profile Card
        Card(
            colors = CardDefaults.cardColors(containerColor = GlassBg),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                // Avatar placeholder
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(colors = listOf(DarkPrimary, DarkSecondary))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (user != null && user.name.isNotEmpty()) user.name.take(2).uppercase() else "CN",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(user?.name ?: "Click 'n Go Client", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(user?.email ?: "preducationbd@gmail.com", color = Color.LightGray, fontSize = 12.sp)
                Text("Phone: +880 " + (user?.id ?: "01712345678"), color = DarkTertiary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }

        // Compliance Card KYC Status
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2E3E)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("eKYC Compliance Status", color = Color.Gray, fontSize = 11.sp)
                    Text(
                        text = user?.kycStatus ?: "UNVERIFIED",
                        color = if (user?.kycStatus == "APPROVED") DarkPrimary else GoldReward,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                if (user?.kycStatus != "APPROVED") {
                    Button(
                        onClick = onVerifyClick,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldReward),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Verify NID", color = Color.DarkGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Icon(Icons.Default.Verified, contentDescription = "Sufficient KYC validations", tint = DarkPrimary)
                }
            }
        }

        // Security controls
        Text("Account Security & Preferences", color = Color.Gray, fontSize = 12.sp)

        Card(
            colors = CardDefaults.cardColors(containerColor = GlassBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                // Biometrics toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null, tint = Color.LightGray)
                        Text("Enable Biometric Fingerprint", color = Color.White, fontSize = 14.sp)
                    }
                    Switch(
                        checked = isBiometrics,
                        onCheckedChange = { checked ->
                            isBiometrics = checked
                            viewModel.triggerEvent("Biometrics settings updated!")
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = DarkPrimary)
                    )
                }

                Divider(color = Color(0x1BFFFFFF))

                // Language toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleLanguage() }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = Color.LightGray)
                        Text("Bespoke Language Switcher", color = Color.White, fontSize = 14.sp)
                    }
                    Text(
                        text = "বাংলা / EN",
                        color = DarkPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Signout Button
        Button(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FF4D4D)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Logout Secure Session", color = FraudFlagColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun WalletBalanceHub(user: UserEntity, viewModel: ClickAndGoViewModel, onConvertClick: () -> Unit) {
    var isVisible by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF03413B),
                            Color(0xFF021B3A)
                        )
                    )
                )
                .border(1.dp, Color(0x3302C382), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = DarkPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Main Multi-Currency Wallet",
                            color = Color.LightGray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Eye visibility Toggle
                    IconButton(
                        onClick = { isVisible = !isVisible },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Show/Hide Balance",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // BDT Ledger
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Local Balance (BDT)",
                            color = Color.LightGray,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = if (isVisible) "৳ ${viewModel.decimalFormat(user.balanceBdt)}" else "৳ ••••••••",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF019B5D))
                            .padding(8.dp)
                    ) {
                        Text("BDT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 14.dp), color = Color(0x22FFFFFF))

                // USD Freelancer Ledger
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "International Freelancer (USD)",
                            color = Color.LightGray,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = if (isVisible) "$ ${viewModel.decimalFormat(user.balanceUsd)}" else "$ ••••••••",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = DarkTertiary
                            )
                        )
                    }

                    Button(
                        onClick = onConvertClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A6CF2)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CurrencyExchange, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Convert", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0x3312222F)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = DarkPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun TransactionRowItem(tx: TransactionEntity, viewModel: ClickAndGoViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0x3312222F)),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0x11FFFFFF))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Category Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            when (tx.type) {
                                "ADD_MONEY" -> Color(0x3302C382)
                                "SEND_MONEY" -> Color(0x333388FF)
                                "CASH_OUT" -> Color(0x33FF4D4D)
                                "UTILITY_BILL" -> Color(0x33FFC107)
                                "MERCHANT_PAYMENT" -> Color(0x33E040FB)
                                "CONVERSION" -> Color(0x3300D2FF)
                                else -> Color(0x339E9E9E)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (tx.type) {
                            "ADD_MONEY" -> Icons.Default.VerticalAlignBottom
                            "SEND_MONEY" -> Icons.Default.Send
                            "CASH_OUT" -> Icons.Default.VerticalAlignTop
                            "UTILITY_BILL" -> Icons.Default.ReceiptLong
                            "MERCHANT_PAYMENT" -> Icons.Default.Storefront
                            "CONVERSION" -> Icons.Default.CurrencyExchange
                            else -> Icons.Default.Payment
                        },
                        contentDescription = tx.type,
                        tint = when (tx.type) {
                            "ADD_MONEY" -> DarkPrimary
                            "SEND_MONEY" -> DarkSecondary
                            "CASH_OUT" -> FraudFlagColor
                            "UTILITY_BILL" -> GoldReward
                            "MERCHANT_PAYMENT" -> Color(0xFFE040FB)
                            "CONVERSION" -> DarkTertiary
                            else -> Color.White
                        }
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    val displayName = if (tx.senderId == viewModel.currentUserId.value) {
                        "To: ${tx.receiverName}"
                    } else {
                        "From: ${tx.senderName}"
                    }
                    Text(
                        displayName,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        tx.description,
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                    Text(
                        viewModel.formatDate(tx.timestamp),
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                val prefix = if (tx.senderId == viewModel.currentUserId.value) "-" else "+"
                val signColor = if (tx.senderId == viewModel.currentUserId.value) FraudFlagColor else DarkPrimary

                Text(
                    text = "$prefix ${tx.currency} ${viewModel.decimalFormat(tx.amount)}",
                    color = signColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                // Render Badge for Status like FLAGGED PENDING
                if (tx.status != "APPROVED") {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when (tx.status) {
                                    "PENDING" -> Color(0x33FFC107)
                                    "FLAGGED_FRAUD" -> Color(0x33FF4D4D)
                                    "REJECTED" -> Color(0x16FFFFFF)
                                    else -> Color.Transparent
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tx.status,
                            color = when (tx.status) {
                                "PENDING" -> GoldReward
                                "FLAGGED_FRAUD" -> FraudFlagColor
                                "REJECTED" -> Color.LightGray
                                else -> Color.White
                            },
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// PORTAL - MERCHANT DASHBOARD
// ==========================================
@Composable
fun MerchantDashboardLayout(viewModel: ClickAndGoViewModel) {
    val activeMerchantId by viewModel.currentUserId.collectAsState()
    val merchantProfile by viewModel.dao.getMerchantByIdFlow(activeMerchantId).collectAsState(initial = null)
    val userObj by viewModel.currentUser.collectAsState(initial = null)
    val transactions by viewModel.allTransactions.collectAsState(initial = emptyList())

    var settlementAmountInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Merchant Dashboard Hub",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
            )
            merchantProfile?.let { m ->
                Text(
                    "Trading Name: ${m.businessName} (Category: ${m.category})",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.LightGray)
                )
            }
        }

        // Ledger Summary
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF003020)),
                border = BorderStroke(1.dp, Color(0x3302C382))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Merchant Ledgers (BDT)", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                        Text(
                            "৳ ${userObj?.let { viewModel.decimalFormat(it.balanceBdt) } ?: "0.00"}",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        merchantProfile?.let { m ->
                            Text(
                                "Settlement Pending ACH: ৳ ${viewModel.decimalFormat(m.settlementRequestedBdt)}",
                                color = DarkSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF02C382)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Store, contentDescription = null, tint = Color(0xFF09121A), modifier = Modifier.size(28.dp))
                    }
                }
            }
        }

        // Stats grid
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = GlassBg)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Today's Credit", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                        merchantProfile?.let { m ->
                            Text("৳ ${viewModel.decimalFormat(m.dailySalesBdt)}", color = DarkPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = GlassBg)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Monthly Credit", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                        merchantProfile?.let { m ->
                            Text("৳ ${viewModel.decimalFormat(m.monthlySalesBdt)}", color = DarkTertiary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                }
            }
        }

        // Settlement Request
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GlassBg),
                border = BorderStroke(1.dp, Color(0x16FFFFFF))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Request ACH Bank Settlement", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Trigger real-time settlement deposit straight to your linked local bank account.", color = Color.LightGray, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = settlementAmountInput,
                            onValueChange = { settlementAmountInput = it },
                            placeholder = { Text("Amount ৳", color = Color.Gray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                val amt = settlementAmountInput.toDoubleOrNull() ?: 0.0
                                if (amt > 0) {
                                    viewModel.requestSettlement(amt)
                                    settlementAmountInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Submit", color = Color(0xFF09121A), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Scan QR display
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GlassBg)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Your Business Payment Point QR", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Drawing a beautiful custom simulated QR matrix
                        Column {
                            repeat(8) { row ->
                                Row {
                                    repeat(8) { col ->
                                        val isFilled = (row % 2 == 0 && col % 3 == 0) || (row % 3 == 1 && col % 2 == 1) || (row < 2 && col < 2) || (row > 5 && col > 5) || (row < 2 && col > 5)
                                        Box(
                                            modifier = Modifier
                                                .size(17.dp)
                                                .background(if (isFilled) Color.Black else Color.White)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    merchantProfile?.let { m ->
                        Text(
                            "Scan to Pay Dhaka Supermart",
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            fontWeight = FontWeight.Bold
                        )
                        Text(m.staticQrCodeData, color = Color.Gray, fontSize = 9.sp)
                    }
                }
            }
        }

        // Merchant credits timeline
        item {
            Text("Business Sales Ledger", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }

        val merchantSalesTxList = transactions.filter { it.receiverId == activeMerchantId }
        if (merchantSalesTxList.isEmpty()) {
            item {
                Text("No sales matching transaction history yet.", color = Color.Gray, modifier = Modifier.padding(12.dp))
            }
        } else {
            items(merchantSalesTxList) { tx ->
                TransactionRowItem(tx, viewModel)
            }
        }
    }
}

// ==========================================
// PORTAL - ADMIN OPS CENTRAL
// ==========================================
@Composable
fun AdminDashboardLayout(viewModel: ClickAndGoViewModel) {
    val transactions by viewModel.allTransactions.collectAsState(initial = emptyList())
    val usersList by viewModel.users.collectAsState(initial = emptyList())
    val counterAntiFraud by viewModel.antiFraudThreshold.collectAsState()
    val autoKyc by viewModel.autoKycApprove.collectAsState()

    var activeTab by remember { mutableStateOf("PENDING_TX") } // PENDING_TX, KYC_VERIFY, SETTLEMENT_ACH, ANTI_FRAUD_TWEAK

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Admin Operations Command Centre",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
            )
            Text(
                "Supervise risks, eKYC applications, ACH settlements & custom fraud limits.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.LightGray)
            )
        }

        // Metrics Row Summary
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GlassBg)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Users", color = Color.LightGray, fontSize = 11.sp)
                        Text("${usersList.size}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Divider(modifier = Modifier.height(30.dp).width(1.dp), color = Color.Gray)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Flagged Tx", color = Color.LightGray, fontSize = 11.sp)
                        val flaggedCount = transactions.filter { it.status == "FLAGGED_FRAUD" || it.status == "PENDING" }.size
                        Text("$flaggedCount", color = FraudFlagColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Divider(modifier = Modifier.height(30.dp).width(1.dp), color = Color.Gray)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("eKYC Queue", color = Color.LightGray, fontSize = 11.sp)
                        val kycQueueCount = usersList.filter { it.kycStatus == "PENDING" }.size
                        Text("$kycQueueCount", color = GoldReward, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }
        }

        // Admin internal tabs selection
        item {
            ScrollableTabRow(
                selectedTabIndex = when (activeTab) {
                    "PENDING_TX" -> 0
                    "KYC_VERIFY" -> 1
                    "SETTLEMENT_ACH" -> 2
                    else -> 3
                },
                containerColor = Color.Transparent,
                divider = {},
                indicator = {}
            ) {
                Tab(
                    selected = activeTab == "PENDING_TX",
                    onClick = { activeTab = "PENDING_TX" },
                    text = { Text("Risky Fraud Flags", color = if (activeTab == "PENDING_TX") DarkPrimary else Color.LightGray) }
                )
                Tab(
                    selected = activeTab == "KYC_VERIFY",
                    onClick = { activeTab = "KYC_VERIFY" },
                    text = { Text("eKYC Approvals", color = if (activeTab == "KYC_VERIFY") DarkPrimary else Color.LightGray) }
                )
                Tab(
                    selected = activeTab == "SETTLEMENT_ACH",
                    onClick = { activeTab = "SETTLEMENT_ACH" },
                    text = { Text("Bank Clearing", color = if (activeTab == "SETTLEMENT_ACH") DarkPrimary else Color.LightGray) }
                )
                Tab(
                    selected = activeTab == "ANTI_FRAUD_TWEAK",
                    onClick = { activeTab = "ANTI_FRAUD_TWEAK" },
                    text = { Text("AML Rules", color = if (activeTab == "ANTI_FRAUD_TWEAK") DarkPrimary else Color.LightGray) }
                )
            }
        }

        when (activeTab) {
            "PENDING_TX" -> {
                val txQueue = transactions.filter { it.status == "FLAGGED_FRAUD" || it.status == "PENDING" }
                if (txQueue.isEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0x0AFFFFFF))) {
                            Text("All transaction triggers cleared. No pending requests or AML risk warnings.", color = Color.LightGray, modifier = Modifier.padding(24.dp), textAlign = TextAlign.Center)
                        }
                    }
                } else {
                    items(txQueue) { tx ->
                        val isDeposit = tx.type == "ADD_MONEY"
                        val headerLabel = if (isDeposit) "PENDING DEPOSIT VERIFICATION" else "FLAGGED RISK - EXCEEDS THRESHOLD"
                        val headerColor = if (isDeposit) DarkPrimary else FraudFlagColor
                        val bgContainerColor = if (isDeposit) Color(0xFF132030) else Color(0xFF261014)
                        val borderColor = if (isDeposit) DarkPrimary.copy(alpha = 0.5f) else FraudFlagColor

                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            border = BorderStroke(1.dp, borderColor),
                            colors = CardDefaults.cardColors(containerColor = bgContainerColor)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text(headerLabel, color = headerColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text("Tx ID: #${tx.txId}", color = Color.Gray, fontSize = 11.sp)
                                }
                                if (isDeposit) {
                                    Text("Depositor: ${tx.receiverName} (${tx.receiverId})", color = Color.White, fontWeight = FontWeight.SemiBold)
                                    Text("Deposit Gateway: ${tx.gateway}", color = Color.White)
                                    Text("Deposit Amount: BDT ${viewModel.decimalFormat(tx.amount)}", color = DarkPrimary, fontWeight = FontWeight.Bold)
                                    Text("Details: ${tx.description}", color = Color.LightGray, fontSize = 11.sp)
                                } else {
                                    Text("Sender: ${tx.senderName} (${tx.senderId})", color = Color.White, fontWeight = FontWeight.SemiBold)
                                    Text("Receiver: ${tx.receiverName} (${tx.receiverId})", color = Color.White)
                                    Text("Trigger Value: BDT ${viewModel.decimalFormat(tx.amount)}", color = FraudFlagColor, fontWeight = FontWeight.SemiBold)
                                    Text("Memo: ${tx.description}", color = Color.LightGray, fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    Button(
                                        onClick = { viewModel.adminResolveTransaction(tx.txId, false) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Text(if (isDeposit) "Reject Deposit" else "Reject & Block", color = Color.White)
                                    }
                                    Button(
                                        onClick = { viewModel.adminResolveTransaction(tx.txId, true) },
                                        colors = ButtonDefaults.buttonColors(containerColor = DarkPrimary)
                                    ) {
                                        Text(if (isDeposit) "Approve & Credit" else "Accept & Release", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "KYC_VERIFY" -> {
                val kycQueue = usersList.filter { it.kycStatus == "PENDING" }
                if (kycQueue.isEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0x0AFFFFFF))) {
                            Text("eKYC Inbox empty. Excellent job!", color = Color.LightGray, modifier = Modifier.padding(24.dp), textAlign = TextAlign.Center)
                        }
                    }
                } else {
                    items(kycQueue) { userItem ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = GlassBg),
                            border = BorderStroke(1.dp, GoldReward)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("PENDING eKYC VALIDATION", color = GoldReward, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text("Name: ${userItem.name}", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Email: ${userItem.email}", color = Color.LightGray)
                                Text("Registered Phone: ${userItem.id}", color = Color.LightGray)
                                Text("NID Document Number: ${userItem.nidNumber}", color = DarkTertiary, fontFamily = FontFamily.Monospace)
                                
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    Button(
                                        onClick = { viewModel.adminApproveKyc(userItem.id, false) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Text("Decline", color = Color.White)
                                    }
                                    Button(
                                        onClick = { viewModel.adminApproveKyc(userItem.id, true) },
                                        colors = ButtonDefaults.buttonColors(containerColor = DarkPrimary)
                                    ) {
                                        Text("Approve", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "SETTLEMENT_ACH" -> {
                val settlementTxs = transactions.filter { it.receiverId == "BANK_SETTLEMENT" && it.status == "PENDING" }
                if (settlementTxs.isEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0x0AFFFFFF))) {
                            Text("Zero outstanding clearing ACH requests. All banks reconciled.", color = Color.LightGray, modifier = Modifier.padding(24.dp), textAlign = TextAlign.Center)
                        }
                    }
                } else {
                    items(settlementTxs) { stx ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = GlassBg)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("OUTSTANDING CLEARING REQUEST", color = DarkTertiary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text("TxID: #${stx.txId}", color = Color.Gray, fontSize = 11.sp)
                                }
                                Text("Merchant Trading Name: ${stx.senderName}", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Merchant Node ID: ${stx.senderId}", color = Color.LightGray)
                                Text("Draft ACH transfer of: ৳ ${viewModel.decimalFormat(stx.amount)}", color = Color.White, fontWeight = FontWeight.SemiBold)
                                Text("Local Settlement Target: Partner Bank clearing house", color = Color.Gray, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    Button(
                                        onClick = { viewModel.adminResolveSettlement(stx.txId, false) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Text("Reject", color = Color.White)
                                    }
                                    Button(
                                        onClick = { viewModel.adminResolveSettlement(stx.txId, true) },
                                        colors = ButtonDefaults.buttonColors(containerColor = DarkPrimary)
                                    ) {
                                        Text("Clear ACH", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "ANTI_FRAUD_TWEAK" -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GlassBg)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Configure Automated AML Limits", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Any single transfer above this trigger value is auto-placed into the security holding tank for manual admin check.", color = Color.LightGray, fontSize = 11.sp)
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                "Live Security Level: BDT ${viewModel.decimalFormat(counterAntiFraud)} Threshold",
                                color = DarkPrimary,
                                fontWeight = FontWeight.Bold
                            )

                            Slider(
                                value = counterAntiFraud.toFloat(),
                                onValueChange = { viewModel.adminSetAntiFraudThreshold(it.toDouble()) },
                                valueRange = 1000f..100000f,
                                colors = SliderDefaults.colors(
                                    thumbColor = DarkPrimary,
                                    activeTrackColor = DarkPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Automatic eKYC Approval", color = Color.White, fontWeight = FontWeight.SemiBold)
                                    Text("Bypass manual audit & welcome users with immediate welcome rewards.", color = Color.Gray, fontSize = 11.sp)
                                }

                                Switch(
                                    checked = autoKyc,
                                    onCheckedChange = { viewModel.autoKycApprove.value = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = DarkPrimary)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TECH VIEW - ARCHITECTURE & API MODEL
// ==========================================
@Composable
fun TechDocsPortalScreen(viewModel: ClickAndGoViewModel) {
    var subTab by remember { mutableStateOf("MYSQL") } // MYSQL, API_ROUTES, LARAVEL, DEPLOY

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Click 'n Go System Architecture",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
            )
            Text(
                "Technical blueprint including Larvavel controllers, schema specifications & production manuals.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.LightGray)
            )
        }

        // Subtabs
        item {
            ScrollableTabRow(
                selectedTabIndex = when (subTab) {
                    "MYSQL" -> 0
                    "API_ROUTES" -> 1
                    "LARAVEL" -> 2
                    else -> 3
                },
                containerColor = Color.Transparent,
                divider = {},
                indicator = {}
            ) {
                Tab(selected = subTab == "MYSQL", onClick = { subTab = "MYSQL" }, text = { Text("MySQL Relational Schema", color = if (subTab == "MYSQL") DarkTertiary else Color.LightGray) })
                Tab(selected = subTab == "API_ROUTES", onClick = { subTab = "API_ROUTES" }, text = { Text("REST API Endpoints", color = if (subTab == "API_ROUTES") DarkTertiary else Color.LightGray) })
                Tab(selected = subTab == "LARAVEL", onClick = { subTab = "LARAVEL" }, text = { Text("Laravel Backend", color = if (subTab == "LARAVEL") DarkTertiary else Color.LightGray) })
                Tab(selected = subTab == "DEPLOY", onClick = { subTab = "DEPLOY" }, text = { Text("Deployment Manual", color = if (subTab == "DEPLOY") DarkTertiary else Color.LightGray) })
            }
        }

        when (subTab) {
            "MYSQL" -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GlassBg)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Database Entities Schema (ERD Model)", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("MySQL production relational specifications aligned with the active App local Room architecture:", color = Color.LightGray, fontSize = 11.sp)
                            
                            Spacer(modifier = Modifier.height(12.dp))

                            CodeBlockHighlight("""
                              -- Clicking 'n Go Unified relational specifications
                              
                              CREATE TABLE users (
                                  mobile VARCHAR(15) PRIMARY KEY,
                                  name VARCHAR(100) NOT NULL,
                                  email VARCHAR(100) UNIQUE,
                                  pin_hash VARCHAR(255) NOT NULL,
                                  kyc_status ENUM('NOT_STARTED', 'PENDING', 'APPROVED', 'REJECTED') DEFAULT 'NOT_STARTED',
                                  nid_number VARCHAR(20) UNIQUE DEFAULT NULL,
                                  referrals_count INT DEFAULT 0,
                                  loyalty_points INT DEFAULT 0,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                              );
                              
                              CREATE TABLE wallets (
                                  wallet_id INT AUTO_INCREMENT PRIMARY KEY,
                                  user_phone VARCHAR(15) REFERENCES users(mobile),
                                  balance_bdt DECIMAL(15, 2) DEFAULT 0.00,
                                  balance_usd DECIMAL(15, 2) DEFAULT 0.00,
                                  updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                              );
                              
                              CREATE TABLE transactions (
                                  tx_uid BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  sender_phone VARCHAR(15),
                                  receiver_phone VARCHAR(15),
                                  amount DECIMAL(15, 2) NOT NULL,
                                  charge DECIMAL(15, 2) DEFAULT 0.00,
                                  currency VARCHAR(3) DEFAULT 'BDT',
                                  tx_type ENUM('SEND', 'CASHIN', 'CASHOUT', 'PAYBILL', 'CONVERSION'),
                                  status ENUM('APPROVED', 'PENDING', 'FLAGGED', 'FAILED'),
                                  is_anti_fraud_locked BOOLEAN DEFAULT FALSE,
                                  timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
                              );
                            """.trimIndent())
                        }
                    }
                }
            }

            "API_ROUTES" -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GlassBg)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Click 'n Go REST Api Blueprint", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Standard secure JSON Web Token (JWT) secured endpoints used by consumer interfaces:", color = Color.LightGray, fontSize = 11.sp)
                            
                            Spacer(modifier = Modifier.height(12.dp))

                            CodeBlockHighlight("""
                              POST  /api/v1/auth/signup         -- Mobile/Email registration
                              POST  /api/v1/auth/verify-otp     -- SMS OTP verify
                              POST  /api/v1/auth/login          -- JWT token fetch
                              
                              GET   /api/v1/wallet/balance      -- BDT/USD status
                              POST  /api/v1/wallet/add-money     -- Card/bKash/Nagad clearing
                              POST  /api/v1/wallet/send-money    -- Real-time peer-to-peer 
                              
                              POST  /api/v1/kyc/submit-nid      -- eKYC document scan
                              GET   /api/v1/merchant/payments   -- Static/Dynamic QR codes
                              POST  /api/v1/admin/unflag-tx     -- Risk clearance console
                            """.trimIndent())
                        }
                    }
                }
            }

            "LARAVEL" -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GlassBg)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Laravel Core Wallet Controller", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Controller managing balance adjustments, ledger audit logs and real-time AML limits:", color = Color.LightGray, fontSize = 11.sp)
                            
                            Spacer(modifier = Modifier.height(12.dp))

                            CodeBlockHighlight("""
                              <?php
                              namespace App\Http\Controllers\Api\V1;
                              
                              use App\Models\User;
                              use App\Models\Transaction;
                              use Illuminate\Http\Request;
                              use DB;
                              
                              class WalletController extends Controller {
                                  public function sendMoney(Request ${'$'}request) {
                                      ${'$'}request->validate([
                                          'receiver_mobile' => 'required',
                                          'amount' => 'required|numeric|min:10',
                                          'pin' => 'required'
                                      ]);
                                      
                                      ${'$'}sender = auth()->user();
                                      ${'$'}receiver = User::where('mobile', ${'$'}request->receiver_mobile)->first();
                                      ${'$'}amount = ${'$'}request->amount;
                                      
                                      // AML Level Check
                                      ${'$'}isFlagged = ${'$'}amount >= 25000.00;
                                      
                                      return DB::transaction(function() use (${'$'}sender, ${'$'}receiver, ${'$'}amount, ${'$'}isFlagged) {
                                          if (${'$'}sender->balance < ${'$'}amount) {
                                              return response()->json(['error' => 'Insufficient Ledger Balances'], 400);
                                          }
                                          
                                          if (${'$'}isFlagged) {
                                              // Freeze transaction for Admin Manual Clearing
                                              Transaction::create([
                                                  'sender' => ${'$'}sender->mobile,
                                                  'receiver' => ${'$'}receiver->mobile,
                                                  'amount' => ${'$'}amount,
                                                  'status' => 'FLAGGED_FRAUD'
                                              ]);
                                              return response()->json(['status' => 'STALLED_FOR_FRAUD_EVALUATION']);
                                          }
                                          
                                          ${'$'}sender->decrement('balance', ${'$'}amount);
                                          ${'$'}receiver->increment('balance', ${'$'}amount);
                                          
                                          // Create audited Transaction entry
                                      });
                                  }
                              }
                            """.trimIndent())
                        }
                    }
                }
            }

            "DEPLOY" -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GlassBg)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Production Cloud Deployment Guide", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Architecture deployment for Laravel, Flutter apps and AWS cluster configurations:", color = Color.LightGray, fontSize = 11.sp)
                            
                            Spacer(modifier = Modifier.height(12.dp))

                            CodeBlockHighlight("""
                              1. AWS Setup:
                                 - ECS Fargate tasks running Laravel container.
                                 - AWS Aurora RDS with MySQL engine for high stability.
                                 - Redis Cluster for lightning-fast cache and concurrent queue processing.
                              
                              2. NGINX Reverse Proxy configuration:
                                 - Direct secure SSL connections on port 443.
                                 - Rate limiting parameters set at 30 requests/min per IP.
                                 
                              3. CI/CD Pipeline:
                                 - Github Actions automated runner.
                                 - Static code scanning via SonarQube checks.
                                 - Automatic artifact bundle builder pushed to Amazon ECR.
                            """.trimIndent())
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CodeBlockHighlight(code: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF070C12))
            .padding(12.dp)
            .horizontalScroll(rememberScrollState())
    ) {
        Text(
            text = code,
            color = DarkPrimary,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
    }
}

// ==========================================
// MOCK NOTIFICATIONS SHEET
// ==========================================
@Composable
fun NotificationOverlay(viewModel: ClickAndGoViewModel, onClose: () -> Unit) {
    val notifications by viewModel.currentNotifications.collectAsState()

    // Clear unreads on entry
    LaunchedEffect(Unit) {
        viewModel.dao.markNotificationsAsRead(viewModel.currentUserId.value)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xD9000000))
            .clickable(onClick = onClose),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.7f)
                .clickable { /* no-op */ },
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, Color(0x33FFFFFF))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("System Inbox Notifications", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Divider(color = Color(0x22FFFFFF), modifier = Modifier.padding(vertical = 8.dp))

                if (notifications.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No messages in your mailbox.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(notifications) { notif ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0x16FFFFFF)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(notif.title, color = DarkPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(notif.message, color = Color.White, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(viewModel.formatDate(notif.timestamp), color = Color.Gray, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SERVICE SHEETS (BOTTOM DIALOG MOCKS)
// ==========================================

@Composable
fun AddMoneySheet(viewModel: ClickAndGoViewModel, onDismiss: () -> Unit) {
    var selectedGateway by remember { mutableStateOf("BKASH") }
    var amountInput by remember { mutableStateOf("5000") }
    var pinValue by remember { mutableStateOf("") }
    var transactionIdInput by remember { mutableStateOf("") }
    var errText by remember { mutableStateOf("") }

    val isManual = selectedGateway in listOf("BKASH", "NAGAD", "ROCKET", "BANK")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xB3000000))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* noop */ },
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Add money to BDT Wallet", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null, tint = Color.White) }
                }

                Text("Select Payment Gateway Source:", color = Color.LightGray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))

                // Gateways
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("BKASH", "NAGAD", "ROCKET", "BANK").forEach { gateway ->
                        val isSel = gateway == selectedGateway
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) DarkPrimary else Color(0x33FFFFFF))
                                .clickable { selectedGateway = gateway }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                gateway,
                                color = if (isSel) Color(0xFF09121A) else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Conditionally render instruction card depending on chosen source
                if (isManual) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF132030)),
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, Color(0xFF28A7EF).copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = DarkPrimary, modifier = Modifier.size(18.dp))
                                Text(
                                    text = if (selectedGateway == "BANK") "Midland Bank Deposit Instructions" else "$selectedGateway Wallet Deposit",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (selectedGateway == "BANK") {
                                Text("Please send/transfer money to our Midland Bank corporate account:", color = Color.LightGray, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                SelectionContainer {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF0A131F))
                                            .padding(10.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                    ) {
                                        Text("Bank: Midland Bank Ltd.", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                        Text("A/C No: 55051750000224", color = DarkPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Account Name: Click 'n Go Corporate Account", color = Color.LightGray, fontSize = 11.sp)
                                    }
                                }
                            } else {
                                Text("Please Transfer/Send Money to our agent number:", color = Color.LightGray, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                SelectionContainer {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF0A131F))
                                            .padding(10.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                    ) {
                                        Text("Mobile Wallet: $selectedGateway", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                        Text("Deposit Number: 01712611504", color = DarkPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Method: Send Money / Cash-out transfer", color = Color.LightGray, fontSize = 11.sp)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Note: Submit your payment Transaction ID below. Admin will verify and dispatch credit to your ledger manually.",
                                color = Color(0xFFA5F3FC),
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Enter Amount (Minimum BDT 50)", color = Color.LightGray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isManual) {
                    OutlinedTextField(
                        value = transactionIdInput,
                        onValueChange = { transactionIdInput = it.uppercase() },
                        label = { Text("Transaction ID / Payment TrxID", color = Color.LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, 
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = DarkPrimary,
                            unfocusedBorderColor = Color.LightGray
                        ),
                        placeholder = { Text("e.g. TRK98374249 or BX83294", color = Color.Gray) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                OutlinedTextField(
                    value = pinValue,
                    onValueChange = { pinValue = it },
                    label = { Text("4-Digit Security PIN Check", color = Color.LightGray) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(errText, color = FraudFlagColor, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val amount = amountInput.toDoubleOrNull() ?: 0.0
                        if (amount < 50) {
                            errText = "Minimum transfer allowed is BDT 50."
                        } else if (isManual && transactionIdInput.trim().length < 4) {
                            errText = "Please enter a valid Transaction ID to verify your payment."
                        } else if (pinValue.isEmpty()) {
                            errText = "PIN is mandatory for ledger security."
                        } else {
                            val ok = viewModel.addMoney(amount, selectedGateway, pinValue, if (isManual) transactionIdInput.trim() else "")
                            if (ok) {
                                onDismiss()
                            } else {
                                errText = "Incorrect security PIN. Please try again!"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkPrimary)
                ) {
                    Text(
                        text = if (isManual) "Submit Request to Verify" else "Confirm Credit Settlement",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SendMoneySheet(viewModel: ClickAndGoViewModel, onDismiss: () -> Unit) {
    var recipientPhone by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }
    var memoInput by remember { mutableStateOf("") }
    var pinValue by remember { mutableStateOf("") }
    var errText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xB3000000))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* noop */ },
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Send Peer-to-Peer Cash", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null, tint = Color.White) }
                }

                Text("Secure Direct click-and-go transfer within Bangladesh wallets.", color = Color.LightGray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = recipientPhone,
                    onValueChange = { recipientPhone = it },
                    label = { Text("Recipient Wallet Mobile (e.g. 01812345678)", color = Color.LightGray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Transfer Amount (BDT)", color = Color.LightGray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = memoInput,
                    onValueChange = { memoInput = it },
                    label = { Text("Memo / Note for Ledger", color = Color.LightGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = pinValue,
                    onValueChange = { pinValue = it },
                    label = { Text("4-Digit Security PIN Verify", color = Color.LightGray) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(errText, color = FraudFlagColor, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val amount = amountInput.toDoubleOrNull() ?: 0.0
                        if (recipientPhone.isEmpty() || amount <= 0) {
                            errText = "Provide correct phone & amount."
                        } else if (pinValue.isEmpty()) {
                            errText = "PIN required."
                        } else {
                            val currentBal = viewModel.currentUser.value?.balanceBdt ?: 0.0
                            if (currentBal < amount) {
                                errText = "Insufficient funds in BDT Ledger!"
                            } else {
                                val ok = viewModel.sendMoney(recipientPhone, amount, pinValue, memoInput)
                                if (ok) {
                                    onDismiss()
                                } else {
                                    errText = "Incorrect security PIN. Try again."
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkPrimary)
                ) {
                    Text("Execute Transfer Protocol", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun VirtualCardsSheet(viewModel: ClickAndGoViewModel, onDismiss: () -> Unit) {
    val cards by viewModel.currentCards.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xB3000000))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .clickable { /* noop */ },
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxSize()
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Instant Virtual Bank Cards", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null, tint = Color.White) }
                }

                Text("Instantly issue and fuel online VISA or Mastercard virtual cards directly using your wallet BDT balance.", color = Color.LightGray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(16.dp))

                if (cards.isEmpty()) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("No active Virtual Cards issued yet.", color = Color.Gray)
                    }
                } else {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(cards) { card ->
                            Card(
                                modifier = Modifier
                                    .width(300.dp)
                                    .height(180.dp),
                                colors = CardDefaults.cardColors(containerColor = if (card.brand == "VISA") Color(0xFF0F1E36) else Color(0xFF38150B)),
                                border = BorderStroke(1.dp, if (card.isActive) DarkPrimary else Color.Gray)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(card.brand, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (card.isActive) Color(0x3302C382) else Color(0x33FF4D4D))
                                                .clickable { viewModel.toggleCardStatus(card.cardId) }
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(if (card.isActive) "ACTIVE" else "FROZEN", color = if (card.isActive) DarkPrimary else FraudFlagColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(card.cardNumber, color = Color.White, fontSize = 18.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text("CARD HOLDER", color = Color.LightGray, fontSize = 8.sp)
                                            Text(card.cardHolder, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("VALID THRU", color = Color.LightGray, fontSize = 8.sp)
                                            Text(card.expiry, color = Color.White, fontSize = 12.sp)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("CVV", color = Color.LightGray, fontSize = 8.sp)
                                            Text(card.cvv, color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { viewModel.createVirtualCard("VISA") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F1E36))
                    ) {
                        Text("Issue Visa Virtual", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.createVirtualCard("MASTERCARD") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38150B))
                    ) {
                        Text("Issue MasterCard", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PayBillsSheet(viewModel: ClickAndGoViewModel, onDismiss: () -> Unit) {
    val billsList by viewModel.bills.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xB3000000))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .clickable { /* noop */ },
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxSize()
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Instant Utility Bill Settlement", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null, tint = Color.White) }
                }

                Text("Avoid late charges! Pay electricity, Gas, WASA bills straight via Click 'n Go.", color = Color.LightGray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(billsList) { bill ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0x11FFFFFF)),
                            border = BorderStroke(1.dp, if (bill.isPaid) DarkPrimary else Color(0x33FFFFFF))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(bill.billingOperator, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text("A/C: ${bill.accountNumber} | Type: ${bill.billType}", color = Color.LightGray, fontSize = 11.sp)
                                    Text("Due: ${bill.dueDate}", color = Color.Gray, fontSize = 10.sp)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("৳ ${viewModel.decimalFormat(bill.amount)}", color = Color.White, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (bill.isPaid) {
                                        Text("PAID", color = DarkPrimary, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    } else {
                                        Button(
                                            onClick = {
                                                viewModel.payBill(bill)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = DarkPrimary),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                            modifier = Modifier.height(28.dp),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("Pay Wallet", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QrPaymentSheet(viewModel: ClickAndGoViewModel, onDismiss: () -> Unit) {
    val merchantsList by viewModel.merchants.collectAsState()
    var selectedMerchantId by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }
    var pinValue by remember { mutableStateOf("") }
    var errText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xB3000000))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* noop */ },
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Scan & Pay Merchants", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null, tint = Color.White) }
                }

                Text("Simulates scan camera capture. Choose from registered sandbox merchants below:", color = Color.LightGray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(12.dp))

                // Merchant custom selector
                merchantsList.forEach { merch ->
                    val isSel = merch.merchantId == selectedMerchantId
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedMerchantId = merch.merchantId }
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isSel) Color(0x3302C382) else Color(0x11FFFFFF))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = if (isSel) DarkPrimary else Color.White)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(merch.businessName, color = Color.White, fontWeight = FontWeight.Bold)
                                Text(merch.category, color = Color.LightGray, fontSize = 11.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Transfer BDT amount to business", color = Color.LightGray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = pinValue,
                    onValueChange = { pinValue = it },
                    label = { Text("4-Digit Secure PIN Verification", color = Color.LightGray) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(errText, color = FraudFlagColor, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val amount = amountInput.toDoubleOrNull() ?: 0.0
                        if (selectedMerchantId.isEmpty() || amount <= 0) {
                            errText = "Select a trading merchant and define BDT value."
                        } else if (pinValue.isEmpty()) {
                            errText = "Security PIN verify required."
                        } else {
                            val ok = viewModel.payMerchant(selectedMerchantId, amount, pinValue)
                            if (ok) {
                                onDismiss()
                            } else {
                                errText = "Incorrect security PIN. Please try again!"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkPrimary)
                ) {
                    Text("Settle QR Payment Block", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun RewardsSheet(viewModel: ClickAndGoViewModel, onDismiss: () -> Unit) {
    val user by viewModel.currentUser.collectAsState(initial = null)
    
    var rotationAngle by remember { mutableStateOf(0f) }
    var isSpinning by remember { mutableStateOf(false) }
    var wonItem by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xB3000000))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .clickable { /* noop */ },
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Cashback & Instant Spin Rewards", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null, tint = Color.White) }
                }

                Text("Earn 5% loyalty points automatically. Spin the Lucky Wheel below for free real-time BDT gifts!", color = Color.LightGray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(14.dp))

                // Stats rows
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0x1F10B981)),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f))
                ) {
                    Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Your Live BDT Balance", color = Color.LightGray, fontSize = 11.sp)
                            Text("৳ ${user?.balanceBdt?.let { viewModel.decimalFormat(it) } ?: "0.00"}", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Club Point Balance", color = Color.LightGray, fontSize = 11.sp)
                            Text("${user?.loyaltyPoints ?: 0} Points", color = GoldReward, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // --- PREMIUM ADAPTIVE LUCKY SPIN WHEEL ---
                Text("🔴 CLICK 'N GO LUCKY SPIN WHEEL 🔴", color = DarkPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .size(230.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F1A24))
                        .border(4.dp, Brush.linearGradient(listOf(DarkPrimary, DarkSecondary)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Draw Wheel segments using rotated Canvas with different vibrant sector styles
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(rotationZ = rotationAngle),
                        contentAlignment = Alignment.Center
                    ) {
                        // We draw 6 beautiful segmented sectors
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = size / 2f
                            val radius = size.minDimension / 2f
                            val sectorColor1 = Color(0xFF1E3A8A) // deep blue
                            val sectorColor2 = Color(0xFF0D9488) // cyan
                            val sectorColor3 = Color(0xFF7C3AED) // purple
                            val sectorColor4 = Color(0xFFD97706) // orange
                            val sectorColor5 = Color(0xFF059669) // emerald
                            val sectorColor6 = Color(0xFFB91C1C) // red

                            val colorsList = listOf(sectorColor1, sectorColor2, sectorColor3, sectorColor4, sectorColor5, sectorColor6)
                            for (i in 0 until 6) {
                                drawArc(
                                    color = colorsList[i],
                                    startAngle = i * 60f,
                                    sweepAngle = 60f,
                                    useCenter = true
                                    // Removed Brush/DrawStyle to keep compilation error-free
                                )
                            }
                        }

                        // Label sectors in BDT or points
                        val labels = listOf("৳2 Cash", "5 Pts", "৳10 Cash", "15 Pts", "৳1 Cash", "Free Gift")
                        labels.forEachIndexed { idx, txt ->
                            val angleRad = Math.toRadians((idx * 60 + 30).toDouble())
                            val x = (Math.cos(angleRad) * 75).toFloat()
                            val y = (Math.sin(angleRad) * 75).toFloat()

                            Text(
                                text = txt,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .offset(x = x.dp, y = y.dp)
                                    .graphicsLayer(rotationZ = (idx * 60 + 120).toFloat())
                            )
                        }
                    }

                    // Static Pointer arrow overlay pointing downward at the top
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 4.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Pointer",
                            tint = Color.White,
                            modifier = Modifier
                                .size(44.dp)
                                .offset(y = (-10).dp)
                        )
                    }

                    // Center Hub Button
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(3.dp, Color(0xFF0B151E), CircleShape)
                            .clickable(enabled = !isSpinning) {
                                scope.launch {
                                    isSpinning = true
                                    wonItem = ""
                                    val targetSpins = (10..18).random() * 360f
                                    val randomDegree = (30..330).random().toFloat()
                                    val targetRotation = targetSpins + randomDegree

                                    val startTime = System.currentTimeMillis()
                                    val duration = 2800 // ms
                                    while (System.currentTimeMillis() - startTime < duration) {
                                        val progress =
                                            (System.currentTimeMillis() - startTime).toFloat() / duration
                                        val easeOut =
                                            1f - (1f - progress) * (1f - progress) * (1f - progress)
                                        rotationAngle = easeOut * targetRotation
                                        kotlinx.coroutines.delay(16)
                                    }
                                    rotationAngle = targetRotation % 360f
                                    isSpinning = false

                                    // Norm landing index
                                    val normalizedAngle = (rotationAngle + 90) % 360
                                    val sectorIdx = (((360 - normalizedAngle) / 60).toInt()) % 6

                                    val cashReward = when (sectorIdx) {
                                        0 -> 2.0
                                        2 -> 10.0
                                        4 -> 1.0
                                        5 -> 5.0 // Free Gift of 5 BDT
                                        else -> 0.0
                                    }
                                    val pointsReward = when (sectorIdx) {
                                        1 -> 5
                                        3 -> 15
                                        else -> 0
                                    }

                                    wonItem = viewModel.winSpinGift(cashReward, pointsReward)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isSpinning) "SPIN..." else "SPIN",
                            color = Color(0xFF0B151E),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }
                }

                if (wonItem.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "🎉 You Won: $wonItem! 🎉",
                        color = DarkPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .background(Color(0xFF0C2C21), RoundedCornerShape(10.dp))
                            .padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Affiliate Box Referral
                Card(colors = CardDefaults.cardColors(containerColor = GlassBg), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Share Referral Invitation Link", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Earn ৳50 reward instantly when friends register & submit NID eKYC.", color = Color.LightGray, fontSize = 11.sp)
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF070C12), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("clickngo.com.bd/ref/${user?.id ?: "017"}", color = DarkPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EkycSheet(viewModel: ClickAndGoViewModel, onDismiss: () -> Unit) {
    var nidNumberInput by remember { mutableStateOf("") }
    var confirmName by remember { mutableStateOf("") }
    var confirmEmail by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(1) } // 1: NID info, 2: Camera Selfie verification upload
    var errText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xB3000000))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* noop */ },
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("eKYC Digital Verification", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null, tint = Color.White) }
                }

                Text("Bangladesh Election Commission digital integration NID verification.", color = Color.LightGray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibility(visible = step == 1) {
                    Column {
                        OutlinedTextField(
                            value = nidNumberInput,
                            onValueChange = { nidNumberInput = it },
                            label = { Text("National ID (NID) Card Number", color = Color.LightGray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = confirmName,
                            onValueChange = { confirmName = it },
                            label = { Text("Confirm Full Legal Printed Name", color = Color.LightGray) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = confirmEmail,
                            onValueChange = { confirmEmail = it },
                            label = { Text("Confirm Contact Email Address", color = Color.LightGray) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (errText.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(errText, color = FraudFlagColor, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (nidNumberInput.length < 10 || confirmName.isEmpty() || confirmEmail.isEmpty()) {
                                    errText = "Provide valid NID Card information to proceed."
                                } else {
                                    errText = ""
                                    step = 2
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkPrimary)
                        ) {
                            Text("Proceed to Active Selfie Check", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                AnimatedVisibility(visible = step == 2) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Active Face Verification Process", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Staring at camera view, blink twice to confirm liveness protocol.", color = Color.LightGray, fontSize = 11.sp)
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        // Drawing a beautiful circular profile simulation
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF070C12))
                                .border(2.dp, DarkPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Face, contentDescription = null, tint = DarkPrimary, modifier = Modifier.size(80.dp))
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                viewModel.submitEkyc(nidNumberInput, confirmName, confirmEmail)
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkPrimary)
                        ) {
                            Text("Confirm & Submit eKYC Audit", color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        TextButton(onClick = { step = 1 }) {
                            Text("Back to document NID config", color = Color.LightGray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CurrencyConvertSheet(viewModel: ClickAndGoViewModel, onDismiss: () -> Unit) {
    val user by viewModel.currentUser.collectAsState(initial = null)
    var isBdtToUsd by remember { mutableStateOf(true) } // Mode: true = BDT to USD, false = USD to BDT
    var amountInput by remember { mutableStateOf("") }
    var errText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xB3000000))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* noop */ },
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Secure Currency Exchange", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null, tint = Color.White) }
                }

                Text("Convert your wallet funds instantly with zero spreads.", color = Color.LightGray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(16.dp))

                // Mode Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { isBdtToUsd = true; amountInput = ""; errText = "" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isBdtToUsd) DarkPrimary else Color(0x22FFFFFF)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("BDT ➔ USD", color = if (isBdtToUsd) Color.Black else Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Button(
                        onClick = { isBdtToUsd = false; amountInput = ""; errText = "" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isBdtToUsd) DarkPrimary else Color(0x22FFFFFF)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("USD ➔ BDT", color = if (!isBdtToUsd) Color.Black else Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(colors = CardDefaults.cardColors(containerColor = Color(0x16FFFFFF)), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Exchange Rate:", color = Color.LightGray, fontSize = 12.sp)
                            Text("1 USD = ৳ ${viewModel.usdToBdtRate} BDT", color = DarkPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Available BDT:", color = Color.LightGray, fontSize = 12.sp)
                            Text("৳ ${user?.balanceBdt?.let { viewModel.decimalFormat(it) } ?: "0.00"}", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Available USD:", color = Color.LightGray, fontSize = 12.sp)
                            Text("$ ${user?.balanceUsd?.let { viewModel.decimalFormat(it) } ?: "0.00"}", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it; errText = "" },
                    label = { 
                        Text(
                            if (isBdtToUsd) "Enter BDT to convert to USD (Min ৳${2 * viewModel.usdToBdtRate})" 
                            else "Enter USD to convert to BDT (Min $2.00)", 
                            color = Color.LightGray, 
                            fontSize = 11.sp
                        ) 
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                if (amountInput.isNotEmpty()) {
                    val inputVal = amountInput.toDoubleOrNull() ?: 0.0
                    val estimateVal = if (isBdtToUsd) inputVal / viewModel.usdToBdtRate else inputVal * viewModel.usdToBdtRate
                    val estimateLabel = if (isBdtToUsd) "$ ${viewModel.decimalFormat(estimateVal)} USD" else "৳ ${viewModel.decimalFormat(estimateVal)} BDT"
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Estimated Credit: $estimateLabel",
                        color = DarkPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                if (errText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(errText, color = FraudFlagColor, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val inputVal = amountInput.toDoubleOrNull() ?: 0.0
                        if (inputVal <= 0) {
                            errText = "Please write a correct positive amount value."
                            return@Button
                        }

                        if (isBdtToUsd) {
                            val usdValue = inputVal / viewModel.usdToBdtRate
                            if (usdValue < 2.0) {
                                errText = "Minimum exchange value is 2.00 USD (৳${2 * viewModel.usdToBdtRate} BDT)."
                            } else if ((user?.balanceBdt ?: 0.0) < inputVal) {
                                errText = "Insufficient local BDT balance."
                            } else {
                                val ok = viewModel.convertBdtToUsd(inputVal)
                                if (ok) onDismiss()
                            }
                        } else {
                            if (inputVal < 2.0) {
                                errText = "Minimum exchange value is 2.00 USD."
                            } else if ((user?.balanceUsd ?: 0.0) < inputVal) {
                                errText = "Insufficient international USD balance."
                            } else {
                                val ok = viewModel.convertUsdToBdt(inputVal)
                                if (ok) onDismiss()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkPrimary)
                ) {
                    Text("Execute Currency Exchange", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LoanRequestSheet(viewModel: ClickAndGoViewModel, onDismiss: () -> Unit) {
    var amountInput by remember { mutableStateOf("15000") }
    var termMonths by remember { mutableStateOf(6) }
    var purposeInput by remember { mutableStateOf("") }
    var errText by remember { mutableStateOf("") }
    var appliedSuccess by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xB3000000))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* noop */ },
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Instant Micro-Loan Application", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null, tint = Color.White) }
                }

                Text("Flexible digital lending for small freelancers and businesses inside Bangladesh.", color = Color.LightGray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(16.dp))

                if (appliedSuccess) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2D21)),
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, Color(0xFF10B981))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = Color(0xFF10B981), modifier = Modifier.size(50.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Application Submitted Successfully!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Your loan request of ৳${viewModel.decimalFormat(amountInput.toDoubleOrNull() ?: 0.0)} BDT is pending approval. You can switch roles to 'ADMIN' in the sandbox console above to instantly review and approve your application!", color = Color.LightGray, fontSize = 12.sp, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Text("Done", color = Color.White)
                            }
                        }
                    }
                } else {
                    // Loan form inputs
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it; errText = "" },
                        label = { Text("Desired Loan Amount (BDT)", color = Color.LightGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Repayment Term duration:", color = Color.LightGray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(3, 6, 12, 24).forEach { term ->
                            val isSelected = termMonths == term
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) DarkPrimary else Color(0x1AFFFFFF))
                                    .clickable { termMonths = term }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "$term Mo",
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = purposeInput,
                        onValueChange = { purposeInput = it },
                        label = { Text("Describe Purpose (e.g. Buy new design laptop)", color = Color.LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Projected interest and calculation details
                    val amt = amountInput.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        val annualRate = 0.09 // 9% affordable micro-credit interest rate
                        val totalInterest = amt * annualRate * (termMonths / 12.0)
                        val totalPayable = amt + totalInterest
                        val emi = totalPayable / termMonths

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1B22)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Lending Terms & Monthly Emi Projection:", color = DarkPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Interest Rate (Affordable micro-credit):", color = Color.LightGray, fontSize = 11.sp)
                                    Text("9% Fixed APR", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Total Cost of Borrowing:", color = Color.LightGray, fontSize = 11.sp)
                                    Text("৳ ${viewModel.decimalFormat(totalInterest)} BDT", color = Color.White, fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Estimated Monthly EMI Payment:", color = Color.LightGray, fontSize = 11.sp)
                                    Text("৳ ${viewModel.decimalFormat(emi)} BDT / Month", color = DarkPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    if (errText.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(errText, color = FraudFlagColor, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val amtVal = amountInput.toDoubleOrNull() ?: 0.0
                            if (amtVal < 1000.0) {
                                errText = "Minimum loan amount request is ৳1,000 BDT."
                            } else if (amtVal > 150000.0) {
                                errText = "Maximum micro-loan threshold limit is ৳150,000 BDT."
                            } else if (purposeInput.isBlank()) {
                                errText = "Please write a brief description of the loan purpose."
                            } else {
                                val ok = viewModel.requestLoan(amtVal, termMonths, purposeInput)
                                if (ok) {
                                    appliedSuccess = true
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkPrimary)
                    ) {
                        Text("Submit Loan Application", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun MobileRechargeSheet(viewModel: ClickAndGoViewModel, onDismiss: () -> Unit) {
    val user by viewModel.currentUser.collectAsState(initial = null)
    var mobileNumber by remember { mutableStateOf("01712345678") }
    var selectedOperator by remember { mutableStateOf("Grameenphone") }
    var amountInput by remember { mutableStateOf("50") }
    var errText by remember { mutableStateOf("") }
    var successMsg by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xB3000000))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* noop */ },
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Instant BD Mobile Recharge", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null, tint = Color.White) }
                }

                Text("Recharge prepaid airtime packages to Grameenphone, Robi, Airtel, Banglalink, or Teletalk SIMs.", color = Color.LightGray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(16.dp))

                if (successMsg.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C2C21)),
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, Color(0xFF10B981))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = Color(0xFF10B981), modifier = Modifier.size(50.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Recharge Successful!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(successMsg, color = Color.LightGray, fontSize = 12.sp, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Text("Done", color = Color.White)
                            }
                        }
                    }
                } else {
                    // Mobile input
                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = { mobileNumber = it; errText = "" },
                        label = { Text("Mobile Number (Bangladeshi 11-digit)", color = Color.LightGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Operator selector grid
                    Text("Select Operator:", color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    val operators = listOf("Grameenphone", "Robi", "Airtel", "Banglalink", "Teletalk")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        operators.forEach { operator ->
                            val isSel = operator == selectedOperator
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) DarkPrimary else Color(0x16FFFFFF))
                                    .border(1.dp, if (isSel) DarkPrimary else Color.Transparent, RoundedCornerShape(6.dp))
                                    .clickable { selectedOperator = operator }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    operator.take(5),
                                    color = if (isSel) Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Amount input or quick buttons
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it; errText = "" },
                        label = { Text("Recharge Amount (BDT)", color = Color.LightGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("20", "50", "100", "200", "500").forEach { quickAmt ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0x1F2A3D4C))
                                    .clickable { amountInput = quickAmt; errText = "" }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("৳$quickAmt", color = DarkPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (errText.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errText, color = FraudFlagColor, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val phone = mobileNumber.trim()
                            val amt = amountInput.toDoubleOrNull() ?: 0.0
                            val u = user ?: return@Button

                            if (phone.length != 11 || !phone.startsWith("01")) {
                                errText = "Please enter a valid 11-digit Bangladeshi mobile number."
                            } else if (amt < 10.0) {
                                errText = "Minimum recharge amount is ৳10 BDT."
                            } else if (amt > u.balanceBdt) {
                                errText = "Insufficient local BDT balance. Available: ৳${viewModel.decimalFormat(u.balanceBdt)}"
                            } else {
                                // Execute recharge deduction block
                                val updatedUser = u.copy(
                                    balanceBdt = u.balanceBdt - amt
                                )
                                scope.launch {
                                    viewModel.dao.insertUser(updatedUser)
                                    viewModel.dao.insertTransaction(TransactionEntity(
                                        senderId = u.id,
                                        senderName = u.name,
                                        receiverId = "RECHARGE_${selectedOperator.uppercase()}",
                                        receiverName = "$selectedOperator E-Recharge",
                                        amount = amt,
                                        currency = "BDT",
                                        charge = 0.0,
                                        type = "UTILITY_RECHARGE",
                                        gateway = "SYSTEM",
                                        status = "APPROVED",
                                        timestamp = System.currentTimeMillis(),
                                        description = "Airtime prepaid recharge sent to $phone ($selectedOperator)"
                                    ))
                                    
                                    viewModel.dao.insertNotification(NotificationEntity(
                                        userId = u.id,
                                        title = "Mobile Recharge Successful",
                                        message = "Your prepaid airtime of ৳${viewModel.decimalFormat(amt)} to $phone has been successfully credited.",
                                        timestamp = System.currentTimeMillis()
                                    ))
                                }
                                successMsg = "৳${viewModel.decimalFormat(amt)} BDT has been credited to $phone ($selectedOperator) successfully!"
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkPrimary)
                    ) {
                        Text("Recharge Airtime Instantly", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// PERSISTENT SANDBOX ROLE CONTROLLER DRAWER
// ==========================================
@Composable
fun SandboxFloatingConsole(viewModel: ClickAndGoViewModel) {
    var expanded by remember { mutableStateOf(false) }
    val activeRole by viewModel.userRoleState.collectAsState()
    val toastMessage = viewModel.uiEvent.collectAsState(initial = "")

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Trigger instant Toast on new message
    LaunchedEffect(toastMessage.value) {
        if (toastMessage.value.isNotEmpty()) {
            android.widget.Toast.makeText(context, toastMessage.value, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(horizontalAlignment = Alignment.End) {
            AnimatedVisibility(
                visible = expanded,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .width(240.dp)
                        .padding(bottom = 8.dp)
                        .shadow(12.dp, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF070F18)),
                    border = BorderStroke(1.dp, Color(0xFF1B2F45))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Developer Sandbox Console",
                            color = Color.LightGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Role Buttons
                        SandboxRoleBtn("Consumer App Portal", activeRole == "CONSUMER", Color(0xFF02C382)) {
                            viewModel.userRoleState.value = "CONSUMER"
                            // switch to customer account
                            viewModel.currentUserId.value = "01712345678"
                        }
                        SandboxRoleBtn("Merchant Dashboard", activeRole == "MERCHANT", Color(0xFF3388FF)) {
                            viewModel.userRoleState.value = "MERCHANT"
                            // switch to merchant account
                            viewModel.currentUserId.value = "01812345678"
                        }
                        SandboxRoleBtn("Admin Central Controls", activeRole == "ADMIN", Color(0xFFFF4D4D)) {
                            viewModel.userRoleState.value = "ADMIN"
                            // switch to admin account
                            viewModel.currentUserId.value = "01912345678"
                        }
                        SandboxRoleBtn("MySQL & Backend REST Docs", activeRole == "TECH_DOCS", Color(0xFFE040FB)) {
                            viewModel.userRoleState.value = "TECH_DOCS"
                        }

                        Divider(color = Color(0x22FFFFFF), modifier = Modifier.padding(vertical = 8.dp))

                        // Seed test user trigger
                        Text(
                            "Simulate eKYC Applicant Trigger",
                            color = Color.Gray,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Button(
                            onClick = {
                                scope.launch {
                                    val testInputPhone = "015" + (10000000..99999999).random()
                                    val testUser = UserEntity(
                                        id = testInputPhone,
                                        name = "Shakib Al Hasan",
                                        email = "shakib@national.bd",
                                        pin = "5566",
                                        kycStatus = "PENDING",
                                        nidNumber = "1987514992813",
                                        nidFrontUri = "nid_sim_front",
                                        selfieUri = "selfie_sim_check",
                                        role = "CONSUMER",
                                        balanceBdt = 1000.0,
                                        balanceUsd = 0.0,
                                        loyaltyPoints = 0,
                                        referralsCount = 0
                                    )
                                    viewModel.dao.insertUser(testUser)
                                    viewModel.triggerEvent("Simulated User $testInputPhone signed up with PENDING eKYC document! Review in Admin Panel.")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x3302C382)),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Spawn applicant", color = DarkPrimary, fontSize = 10.sp)
                        }
                    }
                }
            }

            // Expanding FAB
            FloatingActionButton(
                onClick = { expanded = !expanded },
                containerColor = DarkPrimary,
                contentColor = Color(0xFF09121A),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.Close else Icons.Default.Settings,
                    contentDescription = "Sandbox settings"
                )
            }
        }
    }
}

@Composable
fun SandboxRoleBtn(title: String, active: Boolean, accentColor: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 3.dp),
        colors = CardDefaults.cardColors(containerColor = if (active) Color(0x33FFFFFF) else Color.Transparent)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (active) accentColor else Color.Gray)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, color = if (active) Color.White else Color.Gray, fontSize = 11.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
        }
    }
}
