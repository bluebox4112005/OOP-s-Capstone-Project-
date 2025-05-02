// DOM Elements
const authContainer = document.getElementById('auth-container');
const dashboard = document.getElementById('dashboard');
const loginForm = document.getElementById('login-form');
const signupForm = document.getElementById('signup-form');
const tabButtons = document.querySelectorAll('.tab-btn');
const navLinks = document.querySelectorAll('.nav-links li');
const contentSections = document.querySelectorAll('.content-section');
const logoutBtn = document.getElementById('logout-btn');

// Tab Switching
tabButtons.forEach(button => {
    button.addEventListener('click', () => {
        const tab = button.dataset.tab;
        tabButtons.forEach(btn => btn.classList.remove('active'));
        button.classList.add('active');
        
        document.querySelectorAll('.form').forEach(form => form.classList.remove('active'));
        document.getElementById(`${tab}-form`).classList.add('active');
    });
});

// Navigation
navLinks.forEach(link => {
    link.addEventListener('click', () => {
        const section = link.dataset.section;
        navLinks.forEach(l => l.classList.remove('active'));
        link.classList.add('active');
        
        contentSections.forEach(s => s.classList.remove('active'));
        document.getElementById(section).classList.add('active');
    });
});

// Login Form Handler
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const userId = document.getElementById('login-userId').value;
    const password = document.getElementById('login-password').value;
    
    try {
        const response = await fetch('/api/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ userId, password })
        });
        
        if (response.ok) {
            const data = await response.json();
            showDashboard(data);
        } else {
            showError('Login failed. Please check your credentials.');
        }
    } catch (error) {
        showError('An error occurred. Please try again.');
    }
});

// Signup Form Handler
signupForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const userId = document.getElementById('signup-userId').value;
    const name = document.getElementById('signup-name').value;
    const password = document.getElementById('signup-password').value;
    
    try {
        const response = await fetch('/api/signup', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ userId, name, password })
        });
        
        if (response.ok) {
            showSuccess('Signup successful! Please login.');
            document.querySelector('[data-tab="login"]').click();
        } else {
            showError('Signup failed. User ID might be taken.');
        }
    } catch (error) {
        showError('An error occurred. Please try again.');
    }
});

// Upload Document Handler
document.getElementById('upload-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const name = document.getElementById('doc-name').value;
    const type = document.getElementById('doc-type').value;
    const content = document.getElementById('doc-content').value;
    
    try {
        const response = await fetch('/api/upload', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ name, type, content })
        });
        
        if (response.ok) {
            showSuccess('Document uploaded successfully!');
            loadDocuments();
        } else {
            showError('Upload failed. Please try again.');
        }
    } catch (error) {
        showError('An error occurred. Please try again.');
    }
});

// Create Family Handler
document.getElementById('create-family-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const name = document.getElementById('family-name').value;
    
    try {
        const response = await fetch('/api/create-family', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ name })
        });
        
        if (response.ok) {
            showSuccess('Family created successfully!');
        } else {
            showError('Failed to create family. Please try again.');
        }
    } catch (error) {
        showError('An error occurred. Please try again.');
    }
});

// Add Family Member Handler
document.getElementById('add-member-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const memberId = document.getElementById('member-id').value;
    
    try {
        const response = await fetch('/api/add-member', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ memberId })
        });
        
        if (response.ok) {
            showSuccess('Member added successfully!');
        } else {
            showError('Failed to add member. Please try again.');
        }
    } catch (error) {
        showError('An error occurred. Please try again.');
    }
});

// Logout Handler
logoutBtn.addEventListener('click', () => {
    authContainer.classList.remove('hidden');
    dashboard.classList.add('hidden');
    document.getElementById('login-form').reset();
});

// Helper Functions
function showDashboard(userData) {
    document.getElementById('user-name').textContent = userData.name;
    document.getElementById('user-id').textContent = userData.userId;
    authContainer.classList.add('hidden');
    dashboard.classList.remove('hidden');
    loadDocuments();
}

async function loadDocuments() {
    try {
        const response = await fetch('/api/documents');
        if (response.ok) {
            const documents = await response.json();
            displayDocuments(documents, 'my-documents');
        }
    } catch (error) {
        showError('Failed to load documents.');
    }
}

function displayDocuments(documents, containerId) {
    const container = document.getElementById(containerId);
    container.innerHTML = '';
    
    documents.forEach(doc => {
        const card = document.createElement('div');
        card.className = 'document-card';
        card.innerHTML = `
            <h3>${doc.documentName}</h3>
            <p>Type: ${doc.documentType}</p>
            <p>Uploaded: ${new Date(doc.uploadDate).toLocaleDateString()}</p>
            <button onclick="viewDocument('${doc.documentId}')" class="btn">View</button>
        `;
        container.appendChild(card);
    });
}

function showSuccess(message) {
    const div = document.createElement('div');
    div.className = 'success-message';
    div.textContent = message;
    document.body.appendChild(div);
    setTimeout(() => div.remove(), 3000);
}

function showError(message) {
    const div = document.createElement('div');
    div.className = 'error-message';
    div.textContent = message;
    document.body.appendChild(div);
    setTimeout(() => div.remove(), 3000);
}

async function viewDocument(documentId) {
    try {
        const response = await fetch(`/api/documents/${documentId}`);
        if (response.ok) {
            const document = await response.json();
            // Display document content in a modal or new page
            alert(`Document Content:\n\n${document.content}`);
        } else {
            showError('Failed to load document.');
        }
    } catch (error) {
        showError('An error occurred while loading the document.');
    }
} 