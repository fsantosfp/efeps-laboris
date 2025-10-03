import React from "react";
import ReactDom from 'react-dom/client';
import App from "./App";
import {BrowserRouter} from 'react-router-dom';
import { AuthProvider } from "./context/AuthContext";
import { setOptions } from '@googlemaps/js-api-loader';

const apiKey = import.meta.env.VITE_GOOGLE_MAPS_API_KEY;

setOptions({
  key: apiKey,
  libraries: ['places']
});

ReactDom.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <App />
      </AuthProvider>
    </BrowserRouter>
  </React.StrictMode>
)