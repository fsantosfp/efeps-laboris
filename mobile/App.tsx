import React, { useEffect, useState } from "react";
import { NavigationContainer } from "@react-navigation/native";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import { View, ActivityIndicator } from "react-native";
import LoginScreen from "./src/screens/LoginScreen";
import ChangePasswordScreen from "./src/screens/ChangePasswordScreen";
import AsyncStorage from "@react-native-async-storage/async-storage";
import TabNavigator from "./src/navigation/TabNavigator";
import { Alert } from "react-native";

const Stack = createNativeStackNavigator();

const App = (): React.JSX.Element => {

  const [isLoggedIn, setIsLoggedIn] = useState<boolean | null>(null);
  const [passwordResetRequired, setPasswordResetRequired] = useState<boolean>(false);

  useEffect(() => {
      const checkToken =  async() => {
        try{
          const token = await AsyncStorage.getItem('authToken');
          const resetRequired = await AsyncStorage.getItem('passwordResetRequired');
          setIsLoggedIn(!!token);
          setPasswordResetRequired(resetRequired === 'true');
        } catch (error){
          console.error("Falha ao buscar o token do AsyncStorage:", error)
          setIsLoggedIn(false)
        }
      }
      checkToken();
  }, []);

  const handleLoginSuccess = (resetRequired: boolean) => {
    setPasswordResetRequired(resetRequired);
    setIsLoggedIn(true);
  }

  const handleLogout = async () => {
    try {
      await AsyncStorage.removeItem('authToken');
      await AsyncStorage.removeItem('passwordResetRequired');
      setPasswordResetRequired(false);
      setIsLoggedIn(false);
    } catch (error) {
        console.error("Falha ao remover o token:", error);
        setIsLoggedIn(false);
    }
  }

  const handlePasswordChanged = async () => {
    try {
      await AsyncStorage.removeItem('authToken');
      await AsyncStorage.removeItem('passwordResetRequired');
      setPasswordResetRequired(false);
      setIsLoggedIn(false);
      Alert.alert("Sucesso", "Senha alterada com sucesso! Faça login com sua nova senha.");
    } catch (error) {
        console.error("Falha ao limpar sessão após alteração de senha:", error);
    }
  }

  if( isLoggedIn === null ){
    return <View style={{flex: 1, justifyContent: 'center'}}><ActivityIndicator size="large" /></View>;
  }

  return (
    <NavigationContainer>
      <Stack.Navigator>
        {isLoggedIn ? (
          passwordResetRequired ? (
            <Stack.Screen name="ChangePassword" options={{ headerShown: false, gestureEnabled: false }}>
              {(props) => <ChangePasswordScreen {...props} onPasswordChanged={handlePasswordChanged} />}
            </Stack.Screen>
          ) : (
            <Stack.Screen name="MainApp" options={{ headerShown: false }} >
              { () => <TabNavigator onLogout={handleLogout} />}
            </Stack.Screen>
          )
        ) : (
          <Stack.Screen name="Login" options={{ headerShown: false }}>
            {(props) => <LoginScreen {...props} onLoginSucess={handleLoginSuccess}/>}
          </Stack.Screen>
        )}
      </Stack.Navigator>
    </NavigationContainer>
  )
}

export default App;