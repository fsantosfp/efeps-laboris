import React from 'react';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { Image } from 'react-native';
import MainScreen from '../screens/MainScreen';
import HistoryScreen from '../screens/HistoryScreen';
import { theme } from '../styles/theme';

const Tab = createBottomTabNavigator();

const DummyComponent = () => null;

const TabNavigator = ({ onLogout }) => {
    return (
        <Tab.Navigator
            screenOptions={({ route }) => ({
                headerShown: false,
                tabBarActiveTintColor: theme.colors.primary,
                tabBarInactiveTintColor: theme.colors.textMuted,
                tabBarLabelStyle: {
                    fontSize: 12,
                    fontWeight: '600',
                    marginBottom: 4,
                },
                tabBarStyle: {
                    height: 60,
                    paddingBottom: 6,
                    paddingTop: 6,
                    backgroundColor: theme.colors.surface,
                    borderTopWidth: 1,
                    borderTopColor: theme.colors.borderSubtle,
                },
                tabBarIcon: ({ color, size, focused }) => {
                    let iconSource;
                    if (route.name === 'Ponto') {
                        iconSource = require('../assets/ponto_icon.png');
                    } else if (route.name === 'Histórico') {
                        iconSource = require('../assets/historico_icon.png');
                    } else if (route.name === 'Sair') {
                        iconSource = require('../assets/sair_icon.png');
                    }
                    return (
                        <Image 
                            source={iconSource} 
                            style={{ width: 22, height: 22, tintColor: color }} 
                            resizeMode="contain"
                        />
                    );
                }
            })}
        >
            <Tab.Screen name='Ponto'>
                { (props) => <MainScreen {...props} /> }
            </Tab.Screen>
            <Tab.Screen name='Histórico' component={HistoryScreen}/>
            <Tab.Screen 
                name='Sair' 
                component={DummyComponent}
                listeners={{
                    tabPress: (e) => {
                        e.preventDefault();
                        onLogout();
                    }
                }}
            />
        </Tab.Navigator>
    )
}

export default TabNavigator;