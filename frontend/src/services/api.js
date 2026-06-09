import axios from 'axios';

const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1'
});

api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('authToken');

        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        return config;
    },
    (error) => {
        return Promise.reject(error)
    }
);

let isAuthModalOpen = false;

api.interceptors.response.use(
    (response) => {
        return response;
    },
    (error) => {
        if (error.response && error.response.status === 401) {
            if (!isAuthModalOpen) {
                isAuthModalOpen = true;

                // Create overlay
                const overlay = document.createElement('div');
                overlay.id = 'auth-error-overlay';
                overlay.style.position = 'fixed';
                overlay.style.top = '0';
                overlay.style.left = '0';
                overlay.style.width = '100vw';
                overlay.style.height = '100vh';
                overlay.style.backgroundColor = 'rgba(15, 23, 42, 0.6)';
                overlay.style.backdropFilter = 'blur(4px)';
                overlay.style.display = 'flex';
                overlay.style.justifyContent = 'center';
                overlay.style.alignItems = 'center';
                overlay.style.zIndex = '99999';
                overlay.style.fontFamily = 'Inter, system-ui, -apple-system, sans-serif';

                // Create modal container
                const modal = document.createElement('div');
                modal.style.backgroundColor = '#ffffff';
                modal.style.borderRadius = '12px';
                modal.style.padding = '24px';
                modal.style.maxWidth = '400px';
                modal.style.width = '90%';
                modal.style.boxShadow = '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)';
                modal.style.display = 'flex';
                modal.style.flexDirection = 'column';
                modal.style.gap = '16px';
                modal.style.border = '1px solid #e2e8f0';

                // Title
                const title = document.createElement('h3');
                title.innerText = 'Sessão Expirada';
                title.style.margin = '0';
                title.style.fontSize = '18px';
                title.style.fontWeight = '700';
                title.style.color = '#0f172a';

                // Description
                const desc = document.createElement('p');
                desc.innerText = 'Para garantir a segurança dos seus dados, por favor, realize o login novamente.';
                desc.style.margin = '0';
                desc.style.fontSize = '14.5px';
                desc.style.color = '#475569';
                desc.style.lineHeight = '1.5';

                // Action Buttons
                const actions = document.createElement('div');
                actions.style.display = 'flex';
                actions.style.justifyContent = 'flex-end';
                actions.style.gap = '12px';
                actions.style.marginTop = '8px';

                // Cancel Button
                const btnCancel = document.createElement('button');
                btnCancel.innerText = 'Fechar';
                btnCancel.style.padding = '10px 16px';
                btnCancel.style.fontSize = '14px';
                btnCancel.style.fontWeight = '600';
                btnCancel.style.border = '1.5px solid #e2e8f0';
                btnCancel.style.borderRadius = '6px';
                btnCancel.style.backgroundColor = '#ffffff';
                btnCancel.style.color = '#475569';
                btnCancel.style.cursor = 'pointer';
                btnCancel.style.transition = 'all 0.2s ease';
                btnCancel.onmouseover = () => btnCancel.style.backgroundColor = '#f8fafc';
                btnCancel.onmouseout = () => btnCancel.style.backgroundColor = '#ffffff';
                btnCancel.onclick = () => {
                    document.body.removeChild(overlay);
                    isAuthModalOpen = false;
                };

                // Login Button
                const btnLogin = document.createElement('button');
                btnLogin.innerText = 'Fazer Login';
                btnLogin.style.padding = '10px 16px';
                btnLogin.style.fontSize = '14px';
                btnLogin.style.fontWeight = '600';
                btnLogin.style.border = 'none';
                btnLogin.style.borderRadius = '6px';
                btnLogin.style.backgroundColor = '#0A52C6';
                btnLogin.style.color = '#ffffff';
                btnLogin.style.cursor = 'pointer';
                btnLogin.style.transition = 'all 0.2s ease';
                btnLogin.onmouseover = () => btnLogin.style.backgroundColor = '#0842a0';
                btnLogin.onmouseout = () => btnLogin.style.backgroundColor = '#0A52C6';
                btnLogin.onclick = () => {
                    document.body.removeChild(overlay);
                    isAuthModalOpen = false;
                    localStorage.removeItem('authToken');
                    localStorage.removeItem('passwordResetRequired');
                    window.location.href = '/login';
                };

                actions.appendChild(btnCancel);
                actions.appendChild(btnLogin);
                modal.appendChild(title);
                modal.appendChild(desc);
                modal.appendChild(actions);
                overlay.appendChild(modal);
                document.body.appendChild(overlay);
            }
        }
        return Promise.reject(error);
    }
);

export default api;