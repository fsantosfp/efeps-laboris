import React from 'react';
import { View, Text, Image, StyleSheet } from 'react-native';
import { theme } from '../styles/theme';

const ScreenHeader = ({ title }) => {
    return (
        <View style={styles.headerContainer}>
            <Image 
                source={require('../assets/mactronLogo.png')} 
                style={styles.logo} 
                resizeMode="contain"
            />
            <Text style={styles.title}>{title}</Text>
        </View>
    );
};

const styles = StyleSheet.create({
    headerContainer: {
        flexDirection: 'row',
        alignItems: 'center',
        backgroundColor: theme.colors.surface,
        paddingHorizontal: theme.spacing.sm,
        paddingVertical: theme.spacing.xs,
        borderBottomWidth: 1,
        borderBottomColor: theme.colors.borderSubtle,
        width: '100%',
        height: 56,
    },
    logo: {
        width: 32,
        height: 32,
        marginRight: theme.spacing.xs,
    },
    title: {
        fontSize: theme.typography.headline.fontSize,
        fontWeight: theme.typography.headline.fontWeight,
        color: theme.colors.textMain,
    }
});

export default ScreenHeader;
