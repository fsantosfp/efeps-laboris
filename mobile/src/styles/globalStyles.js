import { StyleSheet } from 'react-native';
import { theme } from './theme';

export const globalStyles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: theme.colors.canvas,
    padding: theme.spacing.sm,
  },
  card: {
    backgroundColor: theme.colors.surface,
    borderRadius: theme.radius.card,
    borderWidth: 1,
    borderColor: theme.colors.borderSubtle,
    padding: theme.spacing.sm,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.05,
    shadowRadius: 12,
    elevation: 3,
  },
  title: {
    fontSize: theme.typography.headline.fontSize,
    fontWeight: theme.typography.headline.fontWeight,
    color: theme.colors.textMain,
    marginBottom: theme.spacing.xs,
  },
  bodyText: {
    fontSize: theme.typography.body.fontSize,
    color: theme.colors.textMain,
  },
  mutedText: {
    fontSize: theme.typography.caption.fontSize,
    color: theme.colors.textMuted,
  },
  input: {
    backgroundColor: theme.colors.surface,
    borderColor: theme.colors.borderSubtle,
    borderWidth: 1.5,
    borderRadius: theme.radius.control,
    height: 48,
    paddingHorizontal: 12,
    fontSize: theme.typography.body.fontSize,
    color: theme.colors.textMain,
    marginBottom: theme.spacing.sm,
  },
  btn: {
    height: 48,
    borderRadius: theme.radius.control,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 16,
  },
  btnText: {
    fontSize: 16,
    fontWeight: 'bold',
  },
  btnPrimary: {
    backgroundColor: theme.colors.primary,
  },
  btnPrimaryText: {
    color: '#ffffff',
  },
  btnSuccess: {
    backgroundColor: theme.colors.success,
  },
  btnSuccessText: {
    color: '#ffffff',
  },
  btnDanger: {
    backgroundColor: theme.colors.danger,
  },
  btnDangerText: {
    color: '#ffffff',
  },
  btnSecondary: {
    backgroundColor: theme.colors.secondaryContainer,
  },
  btnSecondaryText: {
    color: theme.colors.primary,
  },
});
