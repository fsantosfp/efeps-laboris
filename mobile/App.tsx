import React, { useEffect, useState } from "react";
import { NavigationContainer } from "@react-navigation/native";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import { Text, View, Button, ActivityIndicator } from "react-native";
import LoginScreen from "./src/screens/LoginScreen";
import MainScreen from "./src/screens/MainScreen";
import AsyncStorage from "@react-native-async-storage/async-storage";

const Stack = createNativeStackNavigator();

const App = () => {

  const [isLoggedIn, setIsLoggedIn] = useState<boolean | null>(null);

  useEffect(() => {
      const checkToken =  async() => {
        try{
          const token = await AsyncStorage.getItem('authToken');
          setIsLoggedIn(!!token)
        } catch (error){
          console.error("Falha ao buscar o token do AsyncStorage:", error)
          setIsLoggedIn(false)
        }
      }
      checkToken();
  }, []);

  const handleLogout = async () => {
    try {
      await AsyncStorage.removeItem('authToken');
      setIsLoggedIn(false);
    } catch (error) {
        console.error("Falha ao remover o token:", error);
        setIsLoggedIn(false);
    }
  }

  if( isLoggedIn === null ){
    return <View style={{flex: 1, justifyContent: 'center'}}><ActivityIndicator /></View>;
  }

  return (
    <NavigationContainer>
      <Stack.Navigator>
        {isLoggedIn ? (
          <Stack.Screen name="MainApp" options={{ headerShown: false }} >
            { (props) => <MainScreen {...props} onLogout={ () => setIsLoggedIn(false) } /> }
          </Stack.Screen>
        ) : (
          <Stack.Screen name="Login">
            {(props) => <LoginScreen {...props} onLoginSucess={ () => setIsLoggedIn(true)}/>}
          </Stack.Screen>
        )}
      </Stack.Navigator>
    </NavigationContainer>
  )
}

export default App;