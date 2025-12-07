// ==================== BASE API CONFIG ====================

// Automatically detect context path (e.g. "/automotive_delivery_war_exploded")
const pathParts = window.location.pathname.split('/').filter(Boolean);
const CONTEXT_PATH = pathParts.length > 0 ? '/' + pathParts[0] : '';
// Base URL for all API calls
const API_URL = CONTEXT_PATH + '/api';

let allVehicles = [];
let filteredVehicles = [];
let cart = [];
let currentUser = null;
let userRole = null;
let allDeliveryMen = [];
let allManagers = [];

// Helper to get model year (DB might have null in different fields)
function getModelYear(vehicle) {
    return vehicle.modelYear ?? vehicle.year ?? null;
}

// Car image mapping by carId
const CAR_IMAGE_MAP = {
    1: 'https://www.auto-nix.de/fileadmin/user_upload/bilder/serien/toyota/corolla/tabs/corolla-exterieur.jpg',
    2: 'https://cdn.autohaus.de/thumb_1200x675/media/5172/toyota-yaris-cross-hybrid-130-gr.jpg',
    3: 'https://images.prismic.io/carwow/9ccb4a9f-0f27-4f61-8b0e-4f903d349fa6_LHD+Toyota+Prius+2023+exterior+6.jpg',
    4: 'https://www.autozeitung.de/assets/field/images/toyota-camry-facelift-2024-vorstellung-01.jpg',
    5: 'https://cdn.motor1.com/images/mgl/xqbLkP/s1/foto---toyota-c-hr-2023---immagini---photogallery.jpg',
    6: 'https://cdn.motor1.com/images/mgl/QMegY/s3/toyota-rav4-plug-in-hybrid-2020-im-test.jpg',
    7: 'https://cdn.motor1.com/images/mgl/zx4KoE/s1/2024-toyota-grand-highlander.webp',
    8: 'https://toyota-media.ch/__image/a/1648187/alias/xxl/v/3/c/19/ar/16-9/fn/2023_HILUX%20GR%20SPORT%20Main%20shots%201.jpg',
    9: 'https://global.toyota/pages/news/images/2024/04/18/1330/001.jpg',
    10: 'https://cdn.autohaus.de/thumb_1200x675/media/5172/supra2.jpg'
};

function getVehicleImage(vehicle) {
    return CAR_IMAGE_MAP[vehicle.carId] ||
        'https://placehold.co/800x560/0a0a0a/6b6b6b?text=TOYOTA';
}

// ==================== INIT ====================

window.addEventListener('DOMContentLoaded', () => {
    checkExistingSession();
    loadVehicles();
    loadDeliveryMen();   // For manager assignment dropdown
    loadManagers();
    initScrollEffects();
    updateCartUI();
});

function initScrollEffects() {
    const nav = document.getElementById('mainNav') || document.querySelector('nav');
    if (!nav) return;
    window.addEventListener('scroll', () => {
        if (window.scrollY > 50) nav.classList.add('scrolled');
        else nav.classList.remove('scrolled');
    });
}

// ==================== AUTHENTICATION ====================

async function register() {
    const name = document.getElementById('registerName').value;
    const email = document.getElementById('registerEmail').value;
    const password = document.getElementById('registerPassword').value;
    const role = document.getElementById('registerRole').value;

    if (!name || !email || !password) {
        showNotification('Please fill all fields', 'error');
        return;
    }

    try {
        const response = await fetch(`${API_URL}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, email, password, role })
        });

        const data = await response.json();

        if (response.ok && data.success) {
            // Auto-login after successful registration
            currentUser = {
                id: data.id,
                email: data.email,
                name: data.name,
                role: data.role,
                avatar: data.name?.charAt(0)?.toUpperCase() || 'U'
            };
            userRole = data.role;

            localStorage.setItem('currentUser', JSON.stringify(currentUser));
            updateUIForUserRole();
            closeAuthModal();
            showNotification(`Welcome, ${currentUser.name}! Registration successful.`);

            // Load appropriate dashboard
            if (currentUser.role === 'CLIENT') {
                loadClientOrders();
            } else if (currentUser.role === 'MANAGER') {
                loadAllOrders();
                loadDeliveries();
            }
        } else {
            showNotification(data.error || 'Registration failed', 'error');
        }
    } catch (error) {
        console.error('Registration error:', error);
        showNotification('Registration failed. Please try again.', 'error');
    }
}

async function login() {
    const email = document.getElementById('loginEmail').value;
    const password = document.getElementById('loginPassword').value;

    if (!email || !password) {
        showNotification('Please enter email and password', 'error');
        return;
    }

    try {
        const response = await fetch(`${API_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });

        const data = await response.json();

        if (response.ok && data.success) {
            currentUser = {
                id: data.id,
                email: data.email,
                name: data.name,
                role: data.role,
                avatar: data.name?.charAt(0)?.toUpperCase() || 'U'
            };

            // Optional: clientId from backend
            if (data.clientId) {
                currentUser.clientId = data.clientId;
            }

            userRole = data.role;

            localStorage.setItem('currentUser', JSON.stringify(currentUser));
            updateUIForUserRole();
            closeAuthModal();
            showNotification(`Welcome back, ${currentUser.name}!`);

            if (currentUser.role === 'CLIENT') {
                loadClientOrders();
            } else if (currentUser.role === 'MANAGER') {
                loadAllOrders();
                loadDeliveries();
            }
        } else {
            showNotification(data.error || 'Login failed', 'error');
        }
    } catch (error) {
        console.error('Login error:', error);
        showNotification('Login failed. Please try again.', 'error');
    }
}

function checkExistingSession() {
    const savedUser = localStorage.getItem('currentUser');
    if (savedUser) {
        currentUser = JSON.parse(savedUser);
        userRole = currentUser.role;
        updateUIForUserRole();
    }
}

function logout() {
    currentUser = null;
    userRole = null;
    localStorage.removeItem('currentUser');

    const userMenu = document.getElementById('userMenu');
    const loginButton = document.getElementById('loginButton');

    if (userMenu) userMenu.style.display = 'none';
    if (loginButton) loginButton.style.display = 'block';

    hideAllDashboards();
    showMainView();

    cart = [];
    updateCartUI();

    showNotification('Logged out successfully');
}

function updateUIForUserRole() {
    if (!currentUser || !currentUser.role) {
        const loginBtn = document.getElementById('loginButton');
        const userMenu = document.getElementById('userMenu');

        if (loginBtn) loginBtn.style.display = 'block';
        if (userMenu) userMenu.style.display = 'none';
        return;
    }

    const roleLower = currentUser.role.toLowerCase();

    document.getElementById('userName').textContent = currentUser.name || 'User';
    document.getElementById('userRole').textContent = roleLower;
    document.getElementById('userAvatar').textContent =
        currentUser.name?.charAt(0)?.toUpperCase() || 'U';

    // Role-specific menu visibility (CLIENT & MANAGER only)
    const clientLink = document.getElementById('clientLink');
    const managerLink = document.getElementById('managerLink');
    const deliveryLink = document.getElementById('deliveryLink'); // if still in HTML

    if (clientLink) {
        clientLink.style.display = currentUser.role === 'CLIENT' ? 'block' : 'none';
    }
    if (managerLink) {
        managerLink.style.display = currentUser.role === 'MANAGER' ? 'block' : 'none';
    }
    if (deliveryLink) {
        // Delivery personnel view not used anymore
        deliveryLink.style.display = 'none';
    }

    if (document.getElementById('userMenu')) {
        document.getElementById('userMenu').style.display = 'flex';
    }
    if (document.getElementById('loginButton')) {
        document.getElementById('loginButton').style.display = 'none';
    }
}

// Demo logins for CLIENT & MANAGER
function quickLogin(role) {
    const demoAccounts = {
        'CLIENT': { email: 'client@example.com', password: 'password123' },
        'MANAGER': { email: 'manager@example.com', password: 'password123' }
    };

    const account = demoAccounts[role];
    if (account) {
        document.getElementById('loginEmail').value = account.email;
        document.getElementById('loginPassword').value = account.password;
        setTimeout(login, 100);
    }
}

// ==================== VEHICLE MANAGEMENT ====================

async function loadVehicles() {
    const loading = document.getElementById('loading');
    const error = document.getElementById('error');
    const grid = document.getElementById('vehiclesGrid');

    loading.style.display = 'block';
    error.style.display = 'none';
    grid.innerHTML = '';

    try {
        const response = await fetch(`${API_URL}/cars`);
        if (!response.ok) throw new Error(`HTTP ${response.status}`);

        allVehicles = await response.json();
        loading.style.display = 'none';

        if (allVehicles.length === 0) {
            document.getElementById('noResults').style.display = 'block';
            return;
        }

        filteredVehicles = [...allVehicles];
        displayVehicles(filteredVehicles);
        updateResultsCount();
        document.getElementById('totalVehicles').textContent = allVehicles.length;
    } catch (err) {
        loading.style.display = 'none';
        error.textContent = `Error loading vehicles: ${err.message}`;
        error.style.display = 'block';
    }
}

function displayVehicles(vehicles) {
    const grid = document.getElementById('vehiclesGrid');
    const noResults = document.getElementById('noResults');
    grid.innerHTML = '';

    if (vehicles.length === 0) {
        noResults.style.display = 'block';
        return;
    }
    noResults.style.display = 'none';

    vehicles.forEach(vehicle => {
        const card = document.createElement('article');
        card.className = 'vehicle-card';

        const modelYear = getModelYear(vehicle);
        const isNew = modelYear && modelYear >= 2023;
        const imageUrl = getVehicleImage(vehicle);

        card.innerHTML = `
            <div class="vehicle-image-wrapper">
                <img src="${imageUrl}"
                     alt="${vehicle.modelName || 'Toyota Model'}"
                     style="width:100%;height:100%;object-fit:cover;">
                ${isNew ? '<div class="vehicle-badge">New</div>' : ''}
            </div>
            <div class="vehicle-info">
                <h3 class="vehicle-model">${vehicle.modelName || 'Toyota Model'}</h3>
                <div class="vehicle-year">${modelYear || '—'}</div>
                <div class="vehicle-specs">
                    <div class="spec-item">
                        <span class="spec-label">Weight</span>
                        <span class="spec-value">${vehicle.weight || '—'} kg</span>
                    </div>
                    <div class="spec-item">
                        <span class="spec-label">Warehouse</span>
                        <span class="spec-value">WH-${vehicle.warehouseId || 'N/A'}</span>
                    </div>
                    <div class="spec-item">
                        <span class="spec-label">Factory</span>
                        <span class="spec-value">FC-${vehicle.factoryId || 'N/A'}</span>
                    </div>
                    <div class="spec-item">
                        <span class="spec-label">ID</span>
                        <span class="spec-value">#${vehicle.carId}</span>
                    </div>
                </div>
                <div class="vehicle-price">HKD ${formatPrice(vehicle.price || 0)}</div>
                <div class="price-label">Estimated Price</div>
                <div class="vehicle-actions">
                    <button class="btn-add-cart" onclick="addToCart(${vehicle.carId})">
                        Add to Cart
                    </button>
                    <button class="btn-wishlist" onclick="toggleWishlist(${vehicle.carId}, event)">
                        <i class="far fa-heart"></i>
                    </button>
                </div>
            </div>
        `;

        grid.appendChild(card);
    });
}

function addToCart(carId) {
    if (!currentUser) {
        showNotification('Please login to add vehicles to cart', 'error');
        showAuthModal();
        return;
    }

    if (currentUser.role !== 'CLIENT') {
        showNotification('Only clients can purchase vehicles', 'error');
        return;
    }

    const vehicle = allVehicles.find(v => v.carId === carId);
    if (!vehicle) return;

    const existingItem = cart.find(item => item.carId === carId);
    if (existingItem) existingItem.quantity++;
    else cart.push({ ...vehicle, quantity: 1 });

    updateCartUI();
    showNotification(`${vehicle.modelName} added to cart!`);
}

async function checkout() {
    if (!currentUser) {
        showNotification('Please login to place orders', 'error');
        showAuthModal();
        return;
    }

    if (currentUser.role !== 'CLIENT') {
        showNotification('Only clients can place orders', 'error');
        return;
    }

    if (cart.length === 0) {
        alert('Your cart is empty!');
        return;
    }

    const deliveryAddress = prompt('Please enter delivery address:');
    const customerPhone = prompt('Please enter your phone number:');

    if (!deliveryAddress || !customerPhone) {
        showNotification('Delivery information required', 'error');
        return;
    }

    try {
        console.log('Attempting checkout...');

        const clientId = currentUser.clientId || currentUser.id;
        const carIds = cart.map(item => parseInt(item.carId, 10));

        const orderData = {
            clientId: parseInt(clientId, 10),
            carIds: carIds,
            deliveryAddress,
            customerName: currentUser.name,
            customerPhone
        };

        console.log('Order data:', orderData);

        const response = await fetch(`${API_URL}/orders`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(orderData)
        });

        const responseText = await response.text();
        console.log('Response:', responseText);

        if (response.ok) {
            try {
                const result = JSON.parse(responseText);
                if (result.success && result.order) {
                    showNotification('✅ Order placed successfully! Order #' + result.order.orderId);
                } else {
                    showNotification('✅ Order placed successfully!');
                }
            } catch (e) {
                showNotification('✅ Order placed successfully!');
            }

            cart = [];
            updateCartUI();
            closeCart();
            loadClientOrders();
        } else {
            showNotification('Order failed: ' + responseText, 'error');
        }
    } catch (error) {
        console.error('Checkout error:', error);
        showNotification('Order failed: ' + error.message, 'error');
    }
}

// ==================== CLIENT DASHBOARD ====================

async function loadClientOrders() {
    if (!currentUser) return;
    try {
        const response = await fetch(`${API_URL}/orders/client/${currentUser.id}`);
        const orders = await response.json();
        displayClientOrders(orders);
    } catch (error) {
        console.error('Error loading client orders:', error);
        document.getElementById('clientOrders').innerHTML =
            '<div class="no-results">Error loading orders</div>';
    }
}

function displayClientOrders(orders) {
    const container = document.getElementById('clientOrders');
    if (!orders || orders.length === 0) {
        container.innerHTML = '<div class="no-results">No orders found</div>';
        return;
    }

    container.innerHTML = orders.map(order => `
        <div class="order-item">
            <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:8px;">
                <strong>Order #${order.orderId}</strong>
                <span class="status-badge ${order.status.toLowerCase()}">
                    ${order.status}
                </span>
            </div>
            <div style="font-size:14px;">Date: ${new Date(order.orderDate).toLocaleDateString()}</div>
            <div style="font-size:14px;">Total: HKD ${formatPrice(order.totalAmount)}</div>
            ${order.deliveryMission ? `
                <div style="margin-top:8px;">
                    <strong>Delivery:</strong> ${order.deliveryMission.status}
                    ${order.deliveryMission.deliveryMan
        ? `<br>Driver: ${order.deliveryMission.deliveryMan.staff.person.name}`
        : ''}
                </div>
            ` : ''}
        </div>
    `).join('');
}

// ==================== MANAGER DASHBOARD ====================

async function loadAllOrders() {
    try {
        const response = await fetch(`${API_URL}/orders`);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const orders = await response.json();

        // Show orders that still need a driver:
        // - no delivery mission yet
        // - or mission exists but no deliveryMan assigned
        // Additionally ignore initial seed/demo orders (orderId < 6)
        const actionableOrders = orders.filter(order =>
                order.orderId >= 6 && (
                    !order.deliveryMission ||
                    !order.deliveryMission.deliveryMan
                )
        );

        displayAllOrders(actionableOrders);
    } catch (error) {
        console.error('Error loading all orders:', error);
        document.getElementById('inventoryList').innerHTML =
            '<div class="no-results">Error loading orders: ' + error.message + '</div>';
    }
}


// Load all deliveries (missions) and show assigned/in progress/completed
async function loadDeliveries() {
    try {
        const response = await fetch(`${API_URL}/delivery`);
        if (!response.ok) throw new Error(`HTTP ${response.status}`);

        const missions = await response.json();

        // Counts:
        // Pending = PENDING + ASSIGNED
        const pendingCount = missions.filter(m => {
            const s = normalizeStatus(m.status);
            return s === 'PENDING' || s === 'ASSIGNED';
        }).length;

        const inProgressCount = missions.filter(m => normalizeStatus(m.status) === 'IN_PROGRESS').length;
        const completedCount = missions.filter(m => normalizeStatus(m.status) === 'COMPLETED').length;

        document.getElementById('pendingMissions').textContent = pendingCount;
        document.getElementById('inProgressMissions').textContent = inProgressCount;
        document.getElementById('completedMissions').textContent = completedCount;

        // Show deliveries that are assigned / in progress / completed
        const filtered = missions.filter(m => {
            const s = normalizeStatus(m.status);
            return s === 'ASSIGNED' || s === 'IN_PROGRESS' || s === 'COMPLETED';
        });

        displayDeliveries(filtered);
    } catch (error) {
        console.error('Error loading missions:', error);
        document.getElementById('deliveryMissionsList').innerHTML =
            '<div class="no-results">Error loading missions: ' + error.message + '</div>';

        document.getElementById('pendingMissions').textContent = '0';
        document.getElementById('inProgressMissions').textContent = '0';
        document.getElementById('completedMissions').textContent = '0';
    }
}

// Hardcoded delivery men (can be replaced with real API later)
async function loadDeliveryMen() {
    allDeliveryMen = [
        { staffId: 7, staff: { person: { name: 'Olivia Driver' } } },
        { staffId: 8, staff: { person: { name: 'Jason Lee' } } }
    ];
}

async function loadManagers() {
    allManagers = [
        { staffId: 5, staff: { person: { name: 'Alex Manager' } } },
        { staffId: 6, staff: { person: { name: 'Rachel Green' } } }
    ];
}

function displayAllOrders(orders) {
    const container = document.getElementById('inventoryList');
    container.innerHTML = '';

    if (!orders || orders.length === 0) {
        container.innerHTML = '<div class="no-results">No unassigned orders</div>';
        return;
    }

    container.innerHTML = orders.map(order => {
        const clientName =
            order.client && order.client.person && order.client.person.name
                ? order.client.person.name
                : `Client #${order.clientId}`;

        return `
        <div class="order-item">
            <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:8px;">
                <div>
                    <strong>Order #${order.orderId}</strong>
                    <div style="font-size:12px;color:#888;">Client: ${clientName}</div>
                </div>
                <span class="status-badge ${order.status.toLowerCase()}">
                    ${order.status}
                </span>
            </div>
            <div style="font-size:14px;">Date: ${new Date(order.orderDate).toLocaleDateString()}</div>
            <div style="font-size:14px;">Total: HKD ${formatPrice(order.totalAmount)}</div>

            <div style="margin-top:10px;">
                <select id="deliveryMan-${order.orderId}" class="form-input" style="margin-bottom:5px;">
                    <option value="">Select Driver</option>
                    ${allDeliveryMen.map(dm =>
            `<option value="${dm.staffId}">${dm.staff.person.name}</option>`
        ).join('')}
                </select>
                <button class="btn-primary" onclick="assignDelivery(${order.orderId})" style="width:100%;">
                    Assign Delivery
                </button>
            </div>
        </div>
        `;
    }).join('');
}

function displayDeliveries(missions) {
    const container = document.getElementById('deliveryMissionsList');
    if (!missions || missions.length === 0) {
        container.innerHTML = '<div class="no-results">No deliveries found</div>';
        return;
    }

    container.innerHTML = missions.map(mission => {
        const missionId = mission.missionId || 'N/A';
        const status = mission.status || 'UNKNOWN';
        const statusClass = normalizeStatus(status).toLowerCase(); // e.g. "assigned", "in_progress", "completed"
        const orderId = mission.orderId || (mission.order && mission.order.orderId) || 'N/A';
        const customerName = mission.customerName || 'Unknown Customer';
        const deliveryAddress = mission.deliveryAddress || 'No address provided';
        const customerPhone = mission.customerPhone || 'No phone';

        return `
        <div class="mission-item">
            <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:8px;">
                <strong>Mission #${missionId}</strong>
                <span class="status-badge ${statusClass}">${status}</span>
            </div>
            <div style="font-size:14px;">Order: #${orderId}</div>
            <div style="font-size:14px;">Customer: ${customerName}</div>
            <div style="font-size:14px;">Address: ${deliveryAddress}</div>
            <div style="font-size:14px;">Phone: ${customerPhone}</div>
        </div>
        `;
    }).join('');
}

async function assignDelivery(orderId) {
    const deliveryManId = document.getElementById(`deliveryMan-${orderId}`).value;
    if (!deliveryManId) {
        showNotification('Please select a driver', 'error');
        return;
    }

    try {
        const managerId = currentUser ? currentUser.id : 5;

        const assignmentData = {
            deliveryManId: parseInt(deliveryManId, 10),
            managerId: parseInt(managerId, 10)
        };

        console.log('Assignment data:', assignmentData);

        const response = await fetch(`${API_URL}/orders/${orderId}/assign-delivery`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(assignmentData)
        });

        const responseText = await response.text();
        console.log('Response:', responseText);

        if (response.ok) {
            showNotification('Delivery assigned successfully!');
            // Reload unassigned orders and deliveries overview
            loadAllOrders();
            loadDeliveries();
        } else {
            showNotification('Assignment failed: ' + responseText, 'error');
        }
    } catch (error) {
        console.error('Assignment error:', error);
        showNotification('Assignment failed: ' + error.message, 'error');
    }
}

// ==================== DASHBOARD NAVIGATION ====================

function showMainView() {
    hideAllDashboards();
    document.getElementById('mainView').style.display = 'block';
    updateNavActiveState('Vehicles');
}

function showClientDashboard() {
    hideAllDashboards();
    document.getElementById('clientDashboard').style.display = 'block';
    updateNavActiveState('My Orders');
    if (currentUser) {
        document.getElementById('clientName').textContent = currentUser.name;
        loadClientOrders();
    }
}

function showManagerDashboard() {
    hideAllDashboards();
    document.getElementById('managerDashboard').style.display = 'block';
    updateNavActiveState('Management');
    showOrderManagement();
}

function showOrderManagement() {
    document.getElementById('orderManagement').style.display = 'block';
    document.getElementById('deliveryManagement').style.display = 'none';
    loadAllOrders();
}

function showDeliveryManagement() {
    document.getElementById('orderManagement').style.display = 'none';
    document.getElementById('deliveryManagement').style.display = 'block';
    loadDeliveries();
}

function hideAllDashboards() {
    document.getElementById('mainView').style.display = 'none';
    document.getElementById('clientDashboard').style.display = 'none';
    document.getElementById('managerDashboard').style.display = 'none';
}

function updateNavActiveState(text) {
    document.querySelectorAll('.nav-links a').forEach(link => {
        link.classList.remove('active');
        if (link.textContent.trim() === text) link.classList.add('active');
    });
}

// ==================== CART ====================

function updateCartUI() {
    const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
    const badge = document.getElementById('cart-count');
    if (badge) badge.textContent = totalItems;
}

function openCart() {
    renderCart();
    document.getElementById('cartModal').classList.add('active');
}

function closeCart() {
    document.getElementById('cartModal').classList.remove('active');
}

function renderCart() {
    const cartItems = document.getElementById('cartItems');
    const itemCount = document.getElementById('itemCount');
    const cartTotal = document.getElementById('cartTotal');
    const checkoutBtn = document.getElementById('checkoutBtn');

    if (cart.length === 0) {
        cartItems.innerHTML = `
            <div class="empty-cart">
                <div class="empty-cart-icon">🛒</div>
                <h3>Your cart is empty</h3>
                <p>Add some vehicles to get started!</p>
            </div>
        `;
        itemCount.textContent = '0';
        cartTotal.textContent = 'HKD 0';
        checkoutBtn.disabled = true;
        return;
    }

    checkoutBtn.disabled = false;
    const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
    const totalPrice = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);

    itemCount.textContent = totalItems;
    cartTotal.textContent = `HKD ${formatPrice(totalPrice)}`;

    cartItems.innerHTML = cart.map(item => {
        const carImage = getVehicleImage(item);

        return `
        <div class="cart-item">
            <div class="cart-item-image">
                <img src="${carImage}"
                     alt="${item.modelName}"
                     style="width:100%;height:100%;object-fit:cover;">
            </div>
            <div class="cart-item-info">
                <div class="cart-item-name">${item.modelName}</div>
                <div class="cart-item-details">
                    ${getModelYear(item) ?? '—'} | Warehouse ${item.warehouseId} | ID: #${item.carId}
                </div>
                <div class="cart-item-price">HKD ${formatPrice(item.price)} × ${item.quantity}</div>
                <div class="quantity-controls">
                    <button class="qty-btn" onclick="updateQuantity(${item.carId}, -1)">−</button>
                    <span class="qty-display">${item.quantity}</span>
                    <button class="qty-btn" onclick="updateQuantity(${item.carId}, 1)">+</button>
                    <button class="remove-btn" onclick="removeFromCart(${item.carId})">Remove</button>
                </div>
            </div>
        </div>
        `;
    }).join('');
}

function updateQuantity(carId, change) {
    const item = cart.find(i => i.carId === carId);
    if (!item) return;
    const newQuantity = item.quantity + change;
    if (newQuantity < 1) return;
    item.quantity = newQuantity;
    updateCartUI();
    if (document.getElementById('cartModal').classList.contains('active')) {
        renderCart();
    }
}

function removeFromCart(carId) {
    if (!confirm('Are you sure you want to remove this item from your cart?')) return;
    cart = cart.filter(item => item.carId !== carId);
    updateCartUI();
    if (document.getElementById('cartModal').classList.contains('active')) {
        renderCart();
    }
    showNotification('Item removed from cart');
}

// ==================== UTILITIES ====================

function getStatusColor(status) {
    switch (status) {
        case 'PENDING': return '#f39c12';
        case 'CONFIRMED': return '#3498db';
        case 'DELIVERED': return '#2ecc71';
        case 'CANCELLED': return '#e74c3c';
        default: return '#95a5a6';
    }
}

function formatPrice(price) {
    return new Intl.NumberFormat('en-HK').format(price);
}

function showNotification(message, type = 'success') {
    const notification = document.createElement('div');
    notification.className = `notification ${type === 'error' ? 'error' : ''}`;
    notification.textContent = message;
    document.body.appendChild(notification);

    setTimeout(() => notification.classList.add('show'), 100);
    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

// Normalize status string (case-insensitive helper)
function normalizeStatus(status) {
    return (status || '').toUpperCase();
}

// ==================== AUTH MODAL HELPERS ====================

function showAuthModal() {
    document.getElementById('authModal').classList.add('active');
    showLoginForm();
}

function closeAuthModal() {
    document.getElementById('authModal').classList.remove('active');
}

function showLoginForm() {
    document.getElementById('loginForm').style.display = 'block';
    document.getElementById('registerForm').style.display = 'none';
    document.getElementById('authTitle').textContent = 'Login to Your Account';
}

function showRegisterForm() {
    document.getElementById('loginForm').style.display = 'none';
    document.getElementById('registerForm').style.display = 'block';
    document.getElementById('authTitle').textContent = 'Create New Account';
}

// ==================== FILTERS & SORTING ====================

function applyFilters() {
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();
    const warehouseId = document.getElementById('warehouseFilter').value;
    const maxPrice = document.getElementById('priceFilter').value;
    const year = document.getElementById('yearFilter').value;
    const factoryId = document.getElementById('factoryFilter').value;

    filteredVehicles = allVehicles.filter(vehicle => {
        const matchesSearch = !searchTerm || vehicle.modelName.toLowerCase().includes(searchTerm);
        const matchesWarehouse = !warehouseId || vehicle.warehouseId == warehouseId;
        const matchesPrice = !maxPrice || vehicle.price <= parseFloat(maxPrice);
        const matchesYear = !year || getModelYear(vehicle) == year;
        const matchesFactory = !factoryId || vehicle.factoryId == factoryId;
        return matchesSearch && matchesWarehouse && matchesPrice && matchesYear && matchesFactory;
    });

    displayVehicles(filteredVehicles);
    updateResultsCount();
}

function resetFilters() {
    document.getElementById('searchInput').value = '';
    document.getElementById('warehouseFilter').value = '';
    document.getElementById('priceFilter').value = '';
    document.getElementById('yearFilter').value = '';
    document.getElementById('factoryFilter').value = '';
    document.getElementById('sortSelect').value = 'default';

    filteredVehicles = [...allVehicles];
    displayVehicles(filteredVehicles);
    updateResultsCount();
}

function sortVehicles() {
    const sortBy = document.getElementById('sortSelect').value;

    switch (sortBy) {
        case 'price-asc':
            filteredVehicles.sort((a, b) => a.price - b.price);
            break;
        case 'price-desc':
            filteredVehicles.sort((a, b) => b.price - a.price);
            break;
        case 'year-desc':
            filteredVehicles.sort((a, b) => (getModelYear(b) || 0) - (getModelYear(a) || 0));
            break;
        case 'year-asc':
            filteredVehicles.sort((a, b) => (getModelYear(a) || 0) - (getModelYear(b) || 0));
            break;
        case 'name-asc':
            filteredVehicles.sort((a, b) => a.modelName.localeCompare(b.modelName));
            break;
        default:
            filteredVehicles = [...allVehicles];
    }

    displayVehicles(filteredVehicles);
}

function updateResultsCount() {
    const count = filteredVehicles.length;
    document.getElementById('resultsCount').textContent =
        `${count} vehicle${count !== 1 ? 's' : ''} available`;
}

function toggleWishlist(carId, event) {
    const button = event.currentTarget;
    button.classList.toggle('active');
    button.innerHTML = button.classList.contains('active')
        ? '<i class="fas fa-heart"></i>'
        : '<i class="far fa-heart"></i>';

    const vehicle = allVehicles.find(v => v.carId === carId);
    if (vehicle) {
        const action = button.classList.contains('active') ? 'added to' : 'removed from';
        showNotification(`${vehicle.modelName} ${action} wishlist!`);
    }
}

// ==================== SIMPLE ABOUT / CONTACT ====================

function showAbout() {
    alert(
        'Toyota Hong Kong - Premium Vehicle Dealership\n\n' +
        'We offer the finest selection of Toyota vehicles with exceptional customer service ' +
        'and comprehensive after-sales support.'
    );
}

function showContact() {
    alert(
        'Contact Us:\n\n' +
        '📍 Address: Fini Hong Kong Headquarters\n' +
        '📞 Phone: +852 3000 0000\n' +
        '📧 Email: info@fini-vehicles.com\n' +
        '🕒 Hours: Mon-Sun 9:00-18:00'
    );
}

// ==================== MODAL CLOSE HANDLERS ====================

document.addEventListener('click', e => {
    if (e.target.classList.contains('modal')) {
        e.target.classList.remove('active');
    }
});

document.addEventListener('keydown', e => {
    if (e.key === 'Escape') {
        document.querySelectorAll('.modal.active')
            .forEach(modal => modal.classList.remove('active'));
    }
});
