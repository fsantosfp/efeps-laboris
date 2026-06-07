import React from 'react';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import MainScreen from '../screens/MainScreen';
import HistoryScreen from '../screens/HistoryScreen';

const Tab = createBottomTabNavigator();

const TabNavigator = ({ onLogout }) => {
    return (
        <Tab.Navigator
            screenOptions={{
                headerShow: false,
                tabBarActiveTintColor: 'blue',
                tabBarInactiveTintColor: 'grey'
            }}
        >
            <Tab.Screen name='Ponto'>
                { (props) => <MainScreen {...props} onLogout={ onLogout }/> }
            </Tab.Screen>
            <Tab.Screen name='Histórico' component={HistoryScreen}/>

        </Tab.Navigator>
    )
}

export default TabNavigator;