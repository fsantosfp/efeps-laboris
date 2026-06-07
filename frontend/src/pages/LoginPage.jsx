import React, { useState } from "react";
import axios from 'axios';
import { useAuth } from "../context/AuthContext";

function LoginPage(){

    const[email, setEmail] = useState('');
    const[password, setPassword] = useState('');
    const[error, setError] = useState('');
    const {login} = useAuth();

    const handleSubmit = async (event) => {
        event.preventDefault();
        setError('');

        try{
            const response = await axios.post('http://localhost:8080/api/v1/auth/login',{
                email: email,
                password: password
            });

            login(response.data.token, response.data.passwordResetRequired);

        } catch (err) {
            console.error("Erro no login: ", err);
            setError('E-mail ou senha inválidos. Por favor, tente novamente.');
        }
    };

    return (
        <div>
            <h2>Login - Laboris</h2>
            <form onSubmit={handleSubmit}>
                <div>
                    <label>E-mail:</label>
                    <input 
                        type="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                    />
                </div>
                <div>
                    <label>Senha:</label>
                    <input
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    />
                </div>
                {error && <p style={{ color: 'red'}}>{error}</p>}
                <button type="submit">Entrar</button>
            </form>
        </div>
    );
}

export default LoginPage;