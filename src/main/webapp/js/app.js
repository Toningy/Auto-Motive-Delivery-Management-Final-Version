// ==================== BASE API CONFIG ====================
alert("app.js loaded");
// Context-Path automatisch bestimmen (z.B. "/automotive_delivery_war_exploded")
const pathParts = window.location.pathname.split('/').filter(Boolean);
const CONTEXT_PATH = pathParts.length > 0 ? '/' + pathParts[0] : '';
// Basis für alle API-Calls
const API_URL = CONTEXT_PATH + '/api';

let allVehicles = [];
let filteredVehicles = [];
let cart = [];
let currentUser = null;
let userRole = null;
let allDeliveryMen = [];
let allManagers = [];

// Helper für Modelljahr (da in DB evtl. mal null)
function getModelYear(vehicle) {
    return vehicle.modelYear ?? vehicle.year ?? null;
}

// Bild-Zuordnung pro Auto-ID
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
    loadDeliveryMen();
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
                loadPendingMissions();
            } else if (currentUser.role === 'DELIVERY_MAN') {
                loadDeliveryMissions();
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
            
            // IMPORTANT: Store clientId if available
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
                loadPendingMissions();
            } else if (currentUser.role === 'DELIVERY_MAN') {
                loadDeliveryMissions();
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

    document.getElementById('userMenu').style.display = 'none';
    document.getElementById('loginButton').style.display = 'block';

    hideAllDashboards();
    showMainView();

    cart = [];
    updateCartUI();

    showNotification('Logged out successfully');
}

function updateUIForUserRole() {
    // Falls kein User oder die Rolle fehlt → UI nicht aktualisieren
    if (!currentUser || !currentUser.role) {
        // Falls kein Nutzer eingeloggt, Login-Button zeigen
        const loginBtn = document.getElementById('loginButton');
        const userMenu = document.getElementById('userMenu');

        if (loginBtn) loginBtn.style.display = 'block';
        if (userMenu) userMenu.style.display = 'none';
        return;
    }

    // Role kann jetzt sicher gelesen werden
    const roleLower = currentUser.role.toLowerCase();

    document.getElementById('userName').textContent = currentUser.name || 'User';
    document.getElementById('userRole').textContent = roleLower;
    document.getElementById('userAvatar').textContent =
        currentUser.name?.charAt(0)?.toUpperCase() || 'U';

    // Menüpunkt-Visibility je nach Rolle
    document.getElementById('clientLink').style.display =
        currentUser.role === 'CLIENT' ? 'block' : 'none';
    document.getElementById('managerLink').style.display =
        currentUser.role === 'MANAGER' ? 'block' : 'none';
    document.getElementById('deliveryLink').style.display =
        currentUser.role === 'DELIVERY_MAN' ? 'block' : 'none';

    // User menu anzeigen, Login verstecken
    document.getElementById('userMenu').style.display = 'flex';
    document.getElementById('loginButton').style.display = 'none';
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
        console.log("Attempting checkout...");
        
        // Get clientId - either from currentUser or use user id as fallback
        const clientId = currentUser.clientId || currentUser.id;
        
        const carIds = cart.map(item => parseInt(item.carId));
        
        const orderData = {
            clientId: parseInt(clientId),  // Use the real clientId
            carIds: carIds,
            deliveryAddress: deliveryAddress,
            customerName: currentUser.name,
            customerPhone: customerPhone
        };

        console.log("Order data:", orderData);
        
        const response = await fetch(`${API_URL}/orders`, {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(orderData)
        });

        const responseText = await response.text();
        console.log("Response:", responseText);

        if (response.ok) {
            try {
                const result = JSON.parse(responseText);
                if (result.success) {
                    showNotification('✅ Order placed successfully! Order #' + result.order.orderId);
                    cart = [];
                    updateCartUI();
                    closeCart();
                    loadClientOrders();
                } else {
                    showNotification('Order failed: ' + (result.error || 'Unknown error'), 'error');
                }
            } catch (e) {
                showNotification('✅ Order placed successfully!');
                cart = [];
                updateCartUI();
                closeCart();
                loadClientOrders();
            }
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
        const orders = await response.json();
        displayAllOrders(orders);
    } catch (error) {
        console.error('Error loading all orders:', error);
        document.getElementById('inventoryList').innerHTML =
            '<div class="no-results">Error loading orders</div>';
    }
}

async function loadPendingMissions() {
    try {
        const response = await fetch(`${API_URL}/delivery/missions/pending`);
        const missions = await response.json();
        displayPendingMissions(missions);
    } catch (error) {
        console.error('Error loading pending missions:', error);
        document.getElementById('deliveryMissionsList').innerHTML =
            '<div class="no-results">Error loading missions</div>';
    }
}

async function loadDeliveryMen() {
    // Demo-Daten – falls ihr später echtes Endpoint habt, hier ersetzen
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
    if (!orders || orders.length === 0) {
        container.innerHTML = '<div class="no-results">No orders found</div>';
        return;
    }

    container.innerHTML = orders.map(order => `
        <div class="order-item">
            <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:8px;">
                <div>
                    <strong>Order #${order.orderId}</strong>
                    <div style="font-size:12px;color:#888;">Client: ${order.client.person.name}</div>
                </div>
                <span class="status-badge ${order.status.toLowerCase()}">
                    ${order.status}
                </span>
            </div>
            <div style="font-size:14px;">Date: ${new Date(order.orderDate).toLocaleDateString()}</div>
            <div style="font-size:14px;">Total: HKD ${formatPrice(order.totalAmount)}</div>
            ${!order.deliveryMission || order.deliveryMission.status === 'PENDING' ? `
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
            ` : `
                <div style="margin-top:8px;">
                    <strong>Delivery:</strong> ${order.deliveryMission.status}
                    ${order.deliveryMission.deliveryMan
        ? `<br>Driver: ${order.deliveryMission.deliveryMan.staff.person.name}`
        : ''}
                </div>
            `}
        </div>
    `).join('');
}

function displayPendingMissions(missions) {
    const container = document.getElementById('deliveryMissionsList');
    if (!missions || missions.length === 0) {
        container.innerHTML = '<div class="no-results">No pending delivery missions</div>';
        return;
    }

    document.getElementById('pendingMissions').textContent =
        missions.filter(m => m.status === 'PENDING').length;
    document.getElementById('inProgressMissions').textContent =
        missions.filter(m => m.status === 'IN_PROGRESS').length;
    document.getElementById('completedMissions').textContent =
        missions.filter(m => m.status === 'COMPLETED').length;

    container.innerHTML = missions.map(mission => `
        <div class="mission-item">
            <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:8px;">
                <strong>Mission #${mission.missionId}</strong>
                <span class="status-badge pending">${mission.status}</span>
            </div>
            <div style="font-size:14px;">Order: #${mission.order.orderId}</div>
            <div style="font-size:14px;">Customer: ${mission.customerName}</div>
            <div style="font-size:14px;">Address: ${mission.deliveryAddress}</div>
            <div style="font-size:14px;">Phone: ${mission.customerPhone}</div>
        </div>
    `).join('');
}

async function assignDelivery(orderId) {
    const deliveryManId = document.getElementById(`deliveryMan-${orderId}`).value;
    if (!deliveryManId) {
        showNotification('Please select a driver', 'error');
        return;
    }

    try {
        const assignmentData = {
            deliveryManId: parseInt(deliveryManId, 10),
            managerId: currentUser.id
        };

        const response = await fetch(`${API_URL}/orders/${orderId}/assign-delivery`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(assignmentData)
        });

        if (response.ok) {
            showNotification('Delivery assigned successfully!');
            loadAllOrders();
            loadPendingMissions();
        } else {
            const error = await response.text();
            showNotification('Assignment failed: ' + error, 'error');
        }
    } catch (error) {
        showNotification('Assignment failed. Please try again.', 'error');
    }
}

// ==================== DELIVERY PERSONNEL DASHBOARD ====================

async function loadDeliveryMissions() {
    try {
        // Demo: fix auf Fahrer-ID 7
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

    const pendingMissions = missions.filter(m => m.status === 'PENDING' || m.status === 'ASSIGNED');
    const activeMission = missions.find(m => m.status === 'IN_PROGRESS');
    const completedMissions = missions.filter(m => m.status === 'COMPLETED');

    if (pendingMissions.length === 0) {
        pendingContainer.innerHTML = '<div class="no-results">No pending missions</div>';
    } else {
        pendingContainer.innerHTML = pendingMissions.map(mission => `
            <div class="mission-item">
                <div><strong>Mission #${mission.missionId}</strong></div>
                <div style="font-size:12px;">Order: #${mission.order.orderId}</div>
                <div style="font-size:12px;">Customer: ${mission.customerName}</div>
                <div style="font-size:12px;">Address: ${mission.deliveryAddress}</div>
                <button class="btn-primary"
                        onclick="updateMissionStatus(${mission.missionId}, 'IN_PROGRESS')"
                        style="width:100%;margin-top:8px;padding:8px;">
                    Start Mission
                </button>
            </div>
        `).join('');
    }

    if (activeMission) {
        activeContainer.innerHTML = `
            <div style="background:var(--color-bg);padding:15px;border-radius:8px;">
                <div style="font-weight:600;margin-bottom:8px;">Mission #${activeMission.missionId}</div>
                <div style="font-size:14px;margin-bottom:8px;">Order: #${activeMission.order.orderId}</div>
                <div style="font-size:14px;margin-bottom:8px;">Customer: ${activeMission.customerName}</div>
                <div style="font-size:14px;margin-bottom:8px;">Address: ${activeMission.deliveryAddress}</div>
                <div style="font-size:14px;margin-bottom:15px;">Phone: ${activeMission.customerPhone}</div>
                <button class="btn-primary" style="width:100%;"
                        onclick="updateMissionStatus(${activeMission.missionId}, 'COMPLETED')">
                    Mark Complete
                </button>
            </div>
        `;
    } else {
        activeContainer.innerHTML = '<div class="no-results">No active mission</div>';
    }

    if (completedMissions.length === 0) {
        completedContainer.innerHTML = '<div class="no-results">No completed missions</div>';
    } else {
        completedContainer.innerHTML = completedMissions.map(mission => `
            <div class="mission-item">
                <div><strong>Mission #${mission.missionId}</strong></div>
                <div style="font-size:12px;">Order: #${mission.order.orderId}</div>
                <div style="font-size:12px;">Customer: ${mission.customerName}</div>
                <div style="font-size:12px;color:#2ecc71;font-weight:600;">✓ Completed</div>
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

function showDeliveryDashboard() {
    hideAllDashboards();
    document.getElementById('deliveryPersonnelDashboard').style.display = 'block';
    updateNavActiveState('Missions');
    if (currentUser) {
        document.getElementById('deliveryManName').textContent = currentUser.name;
    }
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

    cartItems.innerHTML = cart.map(item => `
        <div class="cart-item">
            <div class="cart-item-image">🚗</div>
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

    setTimeout(() => notification.classList.add('show'), 100);
    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => notification.remove(), 300);
    }, 3000);
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

    switch(sortBy) {
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
