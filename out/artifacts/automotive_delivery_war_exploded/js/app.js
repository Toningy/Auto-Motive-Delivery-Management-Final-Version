// app.js
const API_BASE = window.location.origin + '/automotive-delivery';
const API_URL = API_BASE + '/api';
let allVehicles = [];
let filteredVehicles = [];
let cart = [];
let currentUser = null;
let userRole = null;
let isGridView = true;
let allDeliveryMen = [];
let allManagers = [];

// Initialize application
window.addEventListener('DOMContentLoaded', function() {
    checkExistingSession();
    loadVehicles();
    loadDeliveryMen();
    loadManagers();
    updateHeroStats();
});

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

        if (response.ok) {
            showNotification('Registration successful! Please login.');
            showLoginForm();
        } else {
            showNotification(data.error || 'Registration failed', 'error');
        }
    } catch (error) {
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

        if (response.ok) {
            currentUser = {
                id: data.id,
                email: data.email,
                name: data.name,
                role: data.role,
                avatar: data.name.charAt(0).toUpperCase()
            };
            userRole = data.role;

            localStorage.setItem('currentUser', JSON.stringify(currentUser));
            
            updateUIForUserRole();
            closeAuthModal();
            showNotification(`Welcome back, ${currentUser.name}!`);
            
            // Load user-specific data
            if (currentUser.role === 'CLIENT') {
                loadClientOrders();
            } else if (currentUser.role === 'MANAGER') {
                loadAllOrders();
                loadPendingMissions();
            } else if (currentUser.role === 'DELIVERY_MAN') {
                loadDeliveryMissions();
            }
        } else {
            showNotification(data.error || 'Login failed', 'error');
        }
    } catch (error) {
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
    
    document.getElementById('userMenu').style.display = 'none';
    document.getElementById('loginButton').style.display = 'block';
    
    hideAllDashboards();
    showMainView();
    
    cart = [];
    updateCartUI();
    
    showNotification('Logged out successfully');
}

function updateUIForUserRole() {
    document.getElementById('userName').textContent = currentUser.name;
    document.getElementById('userRole').textContent = currentUser.role.toLowerCase();
    document.getElementById('userAvatar').textContent = currentUser.avatar;
    document.getElementById('userMenu').style.display = 'flex';
    document.getElementById('loginButton').style.display = 'none';

    document.getElementById('clientLink').style.display = currentUser.role === 'CLIENT' ? 'block' : 'none';
    document.getElementById('managerLink').style.display = currentUser.role === 'MANAGER' ? 'block' : 'none';
    document.getElementById('deliveryLink').style.display = currentUser.role === 'DELIVERY_MAN' ? 'block' : 'none';

    if (currentUser.role === 'CLIENT') {
        showClientDashboard();
    } else if (currentUser.role === 'MANAGER') {
        showManagerDashboard();
    } else if (currentUser.role === 'DELIVERY_MAN') {
        showDeliveryDashboard();
    }
}

function quickLogin(role) {
    const demoAccounts = {
        'CLIENT': { email: 'client@example.com', password: 'password123' },
        'MANAGER': { email: 'manager@example.com', password: 'password123' },
        'DELIVERY_MAN': { email: 'delivery@example.com', password: 'password123' }
    };
    
    const account = demoAccounts[role];
    if (account) {
        document.getElementById('loginEmail').value = account.email;
        document.getElementById('loginPassword').value = account.password;
        setTimeout(login, 100);
    }
}

// ==================== VEHICLE & ORDER MANAGEMENT ====================
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
        const card = document.createElement('div');
        card.className = 'vehicle-card';

        const isNew = vehicle.modelYear >= 2023;
        const isFeatured = vehicle.carId % 3 === 0;

        card.innerHTML = `
            <div class="vehicle-image">
                🚗
                ${isNew ? '<div class="vehicle-badge">New</div>' : ''}
                ${isFeatured ? '<div class="vehicle-badge featured">Featured</div>' : ''}

                <div class="quick-actions">
                    <button class="quick-btn" onclick="event.stopPropagation(); addToWishlist(${vehicle.carId})">
                        <i class="far fa-heart"></i>
                    </button>
                    <button class="quick-btn" onclick="event.stopPropagation(); showVehicleDetails(${vehicle.carId})">
                        <i class="far fa-eye"></i>
                    </button>
                </div>
            </div>
            <div class="vehicle-info">
                <div class="vehicle-model">
                    ${vehicle.modelName}
                    <i class="fas fa-share-alt" style="color: #888; cursor: pointer;" onclick="event.stopPropagation(); shareVehicle(${vehicle.carId})"></i>
                </div>
                <div class="vehicle-year">Model Year ${vehicle.modelYear}</div>

                <div class="vehicle-specs">
                    <div class="spec-item">
                        <span class="spec-label">Weight</span>
                        <span class="spec-value">${vehicle.weight} kg</span>
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

                <div class="vehicle-price">
                    ${formatPrice(vehicle.price)}
                    <div class="price-label">HKD</div>
                </div>

                <div class="vehicle-actions">
                    <button class="add-to-cart" onclick="event.stopPropagation(); addToCart(${vehicle.carId})">
                        <i class="fas fa-shopping-cart"></i>
                        <span>Add to Cart</span>
                    </button>
                    <button class="wishlist-btn" onclick="event.stopPropagation(); toggleWishlist(${vehicle.carId})">
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

    if (existingItem) {
        existingItem.quantity++;
    } else {
        cart.push({
            ...vehicle,
            quantity: 1
        });
    }

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

    // Show delivery information form
    const deliveryAddress = prompt('Please enter delivery address:');
    const customerPhone = prompt('Please enter your phone number:');
    
    if (!deliveryAddress || !customerPhone) {
        showNotification('Delivery information required', 'error');
        return;
    }

    try {
        const orderData = {
            clientId: currentUser.id,
            carIds: cart.map(item => item.carId),
            deliveryAddress: deliveryAddress,
            customerName: currentUser.name,
            customerPhone: customerPhone
        };

        const response = await fetch(`${API_URL}/orders`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(orderData)
        });

        if (response.ok) {
            const order = await response.json();
            showNotification('Order placed successfully! Delivery mission created.');
            
            // Clear cart
            cart = [];
            updateCartUI();
            closeCart();
            
            // Reload orders for client
            loadClientOrders();
        } else {
            const error = await response.text();
            showNotification('Order failed: ' + error, 'error');
        }
    } catch (error) {
        showNotification('Order failed. Please try again.', 'error');
    }
}

// ==================== CLIENT DASHBOARD ====================
async function loadClientOrders() {
    try {
        const response = await fetch(`${API_URL}/orders/client/${currentUser.id}`);
        const orders = await response.json();
        displayClientOrders(orders);
    } catch (error) {
        console.error('Error loading client orders:', error);
        document.getElementById('clientOrders').innerHTML = '<div class="no-results">Error loading orders</div>';
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
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                <strong>Order #${order.orderId}</strong>
                <span class="status-badge ${order.status.toLowerCase()}">${order.status}</span>
            </div>
            <div style="color: #666; font-size: 14px;">Date: ${new Date(order.orderDate).toLocaleDateString()}</div>
            <div style="color: #666; font-size: 14px;">Total: HKD ${formatPrice(order.totalAmount)}</div>
        </div>
    `).join('');
}

// ==================== MANAGER DASHBOARD ====================
async function loadAllOrders() {
    try {
        const response = await fetch(`${API_URL}/orders`);
        const orders = await response.json();
        displayAllOrders(orders);
    } catch (error) {
        console.error('Error loading all orders:', error);
        document.getElementById('inventoryList').innerHTML = '<div class="no-results">Error loading orders</div>';
    }
}

async function loadPendingMissions() {
    try {
        const response = await fetch(`${API_URL}/delivery/missions/pending`);
        const missions = await response.json();
        displayPendingMissions(missions);
    } catch (error) {
        console.error('Error loading pending missions:', error);
        document.getElementById('deliveryMissionsList').innerHTML = '<div class="no-results">Error loading missions</div>';
    }
}

async function loadDeliveryMen() {
    try {
        // Mock data - in real app, this would come from backend
        allDeliveryMen = [
            { staffId: 7, name: 'Olivia Driver' },
            { staffId: 8, name: 'Jason Lee' }
        ];
    } catch (error) {
        console.error('Error loading delivery men:', error);
    }
}

async function loadManagers() {
    try {
        // Mock data - in real app, this would come from backend
        allManagers = [
            { staffId: 5, name: 'Alex Manager' },
            { staffId: 6, name: 'Rachel Green' }
        ];
    } catch (error) {
        console.error('Error loading managers:', error);
    }
}

function displayAllOrders(orders) {
    const container = document.getElementById('inventoryList');
    if (!orders || orders.length === 0) {
        container.innerHTML = '<div class="no-results">No orders found</div>';
        return;
    }

    container.innerHTML = orders.map(order => `
        <div class="order-item">
            <div style="display: flex; justify-content: space-between; align-items: start; margin-bottom: 8px;">
                <div>
                    <strong>Order #${order.orderId}</strong>
                    <div style="color: #666; font-size: 12px;">Client ID: ${order.clientId}</div>
                </div>
                <span class="status-badge ${order.status.toLowerCase()}">${order.status}</span>
            </div>
            <div style="color: #666; font-size: 14px;">Date: ${new Date(order.orderDate).toLocaleDateString()}</div>
            <div style="color: #666; font-size: 14px;">Total: HKD ${formatPrice(order.totalAmount)}</div>
        </div>
    `).join('');
}

function displayPendingMissions(missions) {
    const container = document.getElementById('deliveryMissionsList');
    if (!missions || missions.length === 0) {
        container.innerHTML = '<div class="no-results">No pending delivery missions</div>';
        return;
    }

    // Update stats
    document.getElementById('pendingMissions').textContent = missions.filter(m => m.status === 'PENDING').length;
    document.getElementById('inProgressMissions').textContent = missions.filter(m => m.status === 'IN_PROGRESS').length;
    document.getElementById('completedMissions').textContent = missions.filter(m => m.status === 'COMPLETED').length;

    container.innerHTML = missions.map(mission => `
        <div class="mission-item">
            <div style="display: flex; justify-content: space-between; align-items: start; margin-bottom: 8px;">
                <strong>Mission #${mission.missionId}</strong>
                <span class="status-badge pending">${mission.status}</span>
            </div>
            <div style="color: #666; font-size: 14px;">Order: #${mission.orderId}</div>
        </div>
    `).join('');
}

// ==================== DELIVERY PERSONNEL DASHBOARD ====================
async function loadDeliveryMissions() {
    try {
        // For demo, using staffId 7 (Olivia Driver)
        const response = await fetch(`${API_URL}/delivery/missions/delivery-man/7`);
        const missions = await response.json();
        displayDeliveryMissions(missions);
    } catch (error) {
        console.error('Error loading delivery missions:', error);
    }
}

function displayDeliveryMissions(missions) {
    const pendingContainer = document.getElementById('pendingMissionsList');
    const activeContainer = document.getElementById('activeMission');
    const completedContainer = document.getElementById('completedMissionsList');

    const pendingMissions = missions.filter(m => m.status === 'PENDING');
    const activeMission = missions.find(m => m.status === 'IN_PROGRESS');
    const completedMissions = missions.filter(m => m.status === 'COMPLETED');

    // Pending Missions
    if (pendingMissions.length === 0) {
        pendingContainer.innerHTML = '<div class="no-results">No pending missions</div>';
    } else {
        pendingContainer.innerHTML = pendingMissions.map(mission => `
            <div class="mission-item">
                <div><strong>Mission #${mission.missionId}</strong></div>
                <div style="color: #666; font-size: 12px;">Order: #${mission.orderId}</div>
                <button class="btn btn-primary" onclick="updateMissionStatus(${mission.missionId}, 'IN_PROGRESS')" style="width: 100%; margin-top: 8px; padding: 8px;">
                    Start Mission
                </button>
            </div>
        `).join('');
    }

    // Active Mission
    if (activeMission) {
        activeContainer.innerHTML = `
            <div style="background: #e8f6f3; padding: 15px; border-radius: 8px;">
                <div style="font-weight: 600; margin-bottom: 8px;">Mission #${activeMission.missionId}</div>
                <div style="color: #666; font-size: 14px; margin-bottom: 8px;">Order: #${activeMission.orderId}</div>
                <div style="display: flex; gap: 8px;">
                    <button class="btn btn-primary" style="flex: 1;" onclick="updateMissionStatus(${activeMission.missionId}, 'COMPLETED')">
                        Mark Complete
                    </button>
                </div>
            </div>
        `;
    } else {
        activeContainer.innerHTML = '<div class="no-results">No active mission</div>';
    }

    // Completed Missions
    if (completedMissions.length === 0) {
        completedContainer.innerHTML = '<div class="no-results">No completed missions</div>';
    } else {
        completedContainer.innerHTML = completedMissions.map(mission => `
            <div class="mission-item">
                <div><strong>Mission #${mission.missionId}</strong></div>
                <div style="color: #666; font-size: 12px;">Order: #${mission.orderId}</div>
                <div style="color: #2ecc71; font-size: 12px; font-weight: 600;">✓ Completed</div>
            </div>
        `).join('');
    }
}

async function updateMissionStatus(missionId, status) {
    try {
        const response = await fetch(`${API_URL}/delivery/missions/${missionId}/status?status=${status}`, {
            method: 'PUT'
        });

        if (response.ok) {
            showNotification(`Mission status updated to ${status}`);
            loadDeliveryMissions();
        } else {
            showNotification('Status update failed', 'error');
        }
    } catch (error) {
        showNotification('Status update failed', 'error');
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
    document.getElementById('clientName').textContent = currentUser.name;
    loadClientOrders();
}

function showManagerDashboard() {
    hideAllDashboards();
    document.getElementById('managerDashboard').style.display = 'block';
    updateNavActiveState('Management');
    showOrderManagement();
}

function showDeliveryDashboard() {
    hideAllDashboards();
    document.getElementById('deliveryPersonnelDashboard').style.display = 'block';
    updateNavActiveState('Missions');
    document.getElementById('deliveryManName').textContent = currentUser.name;
    loadDeliveryMissions();
}

function showOrderManagement() {
    document.getElementById('orderManagement').style.display = 'block';
    document.getElementById('deliveryManagement').style.display = 'none';
    loadAllOrders();
}

function showDeliveryManagement() {
    document.getElementById('orderManagement').style.display = 'none';
    document.getElementById('deliveryManagement').style.display = 'block';
    loadPendingMissions();
}

function hideAllDashboards() {
    document.getElementById('mainView').style.display = 'none';
    document.getElementById('clientDashboard').style.display = 'none';
    document.getElementById('managerDashboard').style.display = 'none';
    document.getElementById('deliveryPersonnelDashboard').style.display = 'none';
}

function updateNavActiveState(activeLink) {
    document.querySelectorAll('.nav-links a').forEach(link => {
        link.classList.remove('active');
    });

    const activeElement = Array.from(document.querySelectorAll('.nav-links a')).find(link =>
        link.textContent === activeLink
    );

    if (activeElement) {
        activeElement.classList.add('active');
    }
}

// ==================== CART FUNCTIONS ====================
function updateCartUI() {
    const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
    document.getElementById('cart-count').textContent = totalItems;
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

    cartItems.innerHTML = cart.map(item => `
        <div class="cart-item">
            <div class="cart-item-image">🚗</div>
            <div class="cart-item-info">
                <div class="cart-item-name">${item.modelName}</div>
                <div class="cart-item-details">
                    ${item.modelYear} | Warehouse ${item.warehouseId} | ID: #${item.carId}
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
    `).join('');
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
    if (confirm('Are you sure you want to remove this item from your cart?')) {
        cart = cart.filter(item => item.carId !== carId);
        updateCartUI();
        if (document.getElementById('cartModal').classList.contains('active')) {
            renderCart();
        }
        showNotification('Item removed from cart');
    }
}

// ==================== UTILITY FUNCTIONS ====================
function getStatusColor(status) {
    switch(status) {
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

    setTimeout(() => {
        notification.classList.add('show');
    }, 100);

    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => {
            notification.remove();
        }, 300);
    }, 3000);
}

// ==================== UI FUNCTIONS ====================
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

// ==================== VEHICLE FILTERS & DISPLAY ====================
function setGridView() {
    isGridView = true;
    document.getElementById('vehiclesGrid').classList.remove('list-view');
    document.querySelectorAll('.view-btn')[0].classList.add('active');
    document.querySelectorAll('.view-btn')[1].classList.remove('active');
}

function setListView() {
    isGridView = false;
    document.getElementById('vehiclesGrid').classList.add('list-view');
    document.querySelectorAll('.view-btn')[0].classList.remove('active');
    document.querySelectorAll('.view-btn')[1].classList.add('active');
}

function toggleAdvancedFilters() {
    document.getElementById('advancedFilters').classList.toggle('active');
}

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
        const matchesYear = !year || vehicle.modelYear == year;
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
    document.getElementById('advancedFilters').classList.remove('active');

    filteredVehicles = [...allVehicles];
    displayVehicles(filteredVehicles);
    updateResultsCount();
}

function sortVehicles() {
    const sortBy = document.getElementById('sortSelect').value;

    switch(sortBy) {
        case 'price-asc':
            filteredVehicles.sort((a, b) => a.price - b.price);
            break;
        case 'price-desc':
            filteredVehicles.sort((a, b) => b.price - a.price);
            break;
        case 'year-desc':
            filteredVehicles.sort((a, b) => b.modelYear - a.modelYear);
            break;
        case 'year-asc':
            filteredVehicles.sort((a, b) => a.modelYear - b.modelYear);
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
        `${count} vehicle${count !== 1 ? 's' : ''} found`;
}

function updateHeroStats() {
    document.getElementById('totalVehicles').textContent = allVehicles.length;
}

function addToWishlist(carId) {
    const vehicle = allVehicles.find(v => v.carId === carId);
    if (vehicle) {
        showNotification(`${vehicle.modelName} added to wishlist!`);
    }
}

function toggleWishlist(carId) {
    const button = event.currentTarget;
    button.classList.toggle('active');
    button.innerHTML = button.classList.contains('active') ?
        '<i class="fas fa-heart"></i>' : '<i class="far fa-heart"></i>';

    const vehicle = allVehicles.find(v => v.carId === carId);
    if (vehicle) {
        const action = button.classList.contains('active') ? 'added to' : 'removed from';
        showNotification(`${vehicle.modelName} ${action} wishlist!`);
    }
}

function showVehicleDetails(carId) {
    const vehicle = allVehicles.find(v => v.carId === carId);
    if (vehicle) {
        alert(`Vehicle Details:\n\nModel: ${vehicle.modelName}\nYear: ${vehicle.modelYear}\nPrice: HKD ${formatPrice(vehicle.price)}\nWeight: ${vehicle.weight} kg\nWarehouse: ${vehicle.warehouseId}\nFactory: ${vehicle.factoryId}`);
    }
}

function shareVehicle(carId) {
    const vehicle = allVehicles.find(v => v.carId === carId);
    if (vehicle) {
        if (navigator.share) {
            navigator.share({
                title: `${vehicle.modelName} - Toyota Hong Kong`,
                text: `Check out this ${vehicle.modelName} (${vehicle.modelYear}) for HKD ${formatPrice(vehicle.price)}`,
                url: window.location.href
            });
        } else {
            alert(`Share this vehicle: ${vehicle.modelName} (${vehicle.modelYear}) - HKD ${formatPrice(vehicle.price)}`);
        }
    }
}

function showAbout() {
    alert('Toyota Hong Kong - Premium Vehicle Dealership\n\nWe offer the finest selection of Toyota vehicles with exceptional customer service and comprehensive after-sales support.');
}

function showContact() {
    alert('Contact Us:\n\n📍 Address: Toyota Hong Kong Headquarters\n📞 Phone: +852 3000 0000\n📧 Email: info@toyotahk.com\n🕒 Hours: Mon-Sun 9:00-18:00');
}

// Close modal on outside click
document.addEventListener('click', function(e) {
    if (e.target.classList.contains('modal')) {
        e.target.classList.remove('active');
    }
});

// Keyboard shortcuts
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        document.querySelectorAll('.modal.active').forEach(modal => {
            modal.classList.remove('active');
        });
    }
});