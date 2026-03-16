import { Keyboard, Pressable, StyleSheet, Text, View } from 'react-native';
import { AppButton, AppChip, AppInput } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';

type UnitOption = { label: string; value: string };

type Props = {
  styles: any;
  title: string;
  addPlaceholderExtended: string;
  detailsToggleLabel: string;
  detailsHideLabel: string;
  addQuantityPlaceholder: string;
  addItemTitle: string;
  successFeedback?: string | null;
  duplicateTitle?: string | null;
  duplicateSubtitle?: string | null;
  duplicatePrimaryActionLabel?: string | null;
  duplicateSecondaryActionLabel?: string | null;
  unitNoneLabel: string;
  unitToggleMoreLabel: string;
  unitToggleLessLabel: string;
  nameValue: string;
  quantityValue: string;
  addUnit: string | null;
  addError: string | null;
  detailsExpanded: boolean;
  showMoreAddUnits: boolean;
  inputRef: any;
  quantityRef: any;
  primaryUnitOptions: UnitOption[];
  moreUnitOptions: UnitOption[];
  onChangeName: (value: string) => void;
  onSubmitName: () => void | Promise<void>;
  onToggleDetails: () => void;
  onChangeQuantity: (value: string) => void;
  onSelectUnit: (value: string | null) => void;
  onToggleMoreUnits: () => void;
  onAddItem: () => void | Promise<void>;
  onDuplicatePrimaryAction?: () => void | Promise<void>;
  onAddAsNew?: () => void | Promise<void>;
};

export function AddDetailsSheetContent({
  styles,
  title,
  addPlaceholderExtended,
  detailsToggleLabel,
  detailsHideLabel,
  addQuantityPlaceholder,
  addItemTitle,
  successFeedback,
  duplicateTitle,
  duplicateSubtitle,
  duplicatePrimaryActionLabel,
  duplicateSecondaryActionLabel,
  unitNoneLabel,
  unitToggleMoreLabel,
  unitToggleLessLabel,
  nameValue,
  quantityValue,
  addUnit,
  addError,
  detailsExpanded,
  showMoreAddUnits,
  inputRef,
  quantityRef,
  primaryUnitOptions,
  moreUnitOptions,
  onChangeName,
  onSubmitName,
  onToggleDetails,
  onChangeQuantity,
  onSelectUnit,
  onToggleMoreUnits,
  onAddItem,
  onDuplicatePrimaryAction,
  onAddAsNew,
}: Props) {
  const showDuplicatePrompt = !!duplicateTitle && !!duplicatePrimaryActionLabel && !!duplicateSecondaryActionLabel;

  return (
    <>
      <View style={styles.quickAddHeader}>
        <Text style={textStyles.h2}>{title}</Text>
      </View>
      <AppInput
        ref={inputRef}
        placeholder={addPlaceholderExtended}
        value={nameValue}
        onChangeText={onChangeName}
        onSubmitEditing={onSubmitName}
        returnKeyType={nameValue.trim().length > 0 ? 'done' : 'next'}
        autoFocus
      />
      {successFeedback ? (
        <View style={localStyles.successFeedbackRow}>
          <Text style={localStyles.successFeedbackText}>{successFeedback}</Text>
        </View>
      ) : null}
      <Pressable style={({ pressed }) => [localStyles.detailsToggle, pressed ? localStyles.detailsTogglePressed : null]} onPress={onToggleDetails}>
        <Text style={localStyles.detailsToggleLabel}>{detailsExpanded ? detailsHideLabel : detailsToggleLabel}</Text>
      </Pressable>
      {detailsExpanded ? (
        <>
          <View style={localStyles.detailsPanel}>
            <Text style={localStyles.amountLabel}>Quantity</Text>
            <AppInput
              ref={quantityRef}
              value={quantityValue}
              onChangeText={onChangeQuantity}
              placeholder={addQuantityPlaceholder}
              keyboardType="decimal-pad"
              style={styles.addQuantityInput}
            />
            {quantityValue.trim().length > 0 ? (
              <View style={localStyles.unitSection}>
                <View style={styles.addUnitRow}>
                  {primaryUnitOptions.map((unit) => (
                    <AppChip
                      key={unit.value}
                      label={unit.label}
                      active={addUnit === unit.value}
                      accentKey="shopping"
                      onPress={() => {
                        Keyboard.dismiss();
                        onSelectUnit(unit.value);
                      }}
                    />
                  ))}
                  <Pressable
                    style={({ pressed }) => [
                      localStyles.moreUnitsToggle,
                      pressed ? localStyles.moreUnitsTogglePressed : null,
                    ]}
                    onPress={onToggleMoreUnits}
                  >
                    <Text style={localStyles.moreUnitsToggleLabel}>
                      {showMoreAddUnits ? unitToggleLessLabel : unitToggleMoreLabel}
                    </Text>
                  </Pressable>
                </View>
                {showMoreAddUnits ? (
                  <View style={styles.addUnitRow}>
                    {moreUnitOptions.map((unit) => (
                      <AppChip
                        key={unit.value}
                        label={unit.label}
                        active={addUnit === unit.value}
                        accentKey="shopping"
                        onPress={() => {
                          Keyboard.dismiss();
                          onSelectUnit(unit.value);
                        }}
                      />
                    ))}
                  </View>
                ) : null}
                {!addUnit ? (
                  <Pressable
                    style={({ pressed }) => [localStyles.clearUnitRow, pressed ? localStyles.clearUnitRowPressed : null]}
                    onPress={() => {
                      Keyboard.dismiss();
                      onSelectUnit(null);
                    }}
                  >
                    <Text style={localStyles.clearUnitLabel}>{unitNoneLabel}</Text>
                  </Pressable>
                ) : (
                  <Pressable
                    style={({ pressed }) => [localStyles.clearUnitRow, pressed ? localStyles.clearUnitRowPressed : null]}
                    onPress={() => {
                      Keyboard.dismiss();
                      onSelectUnit(null);
                    }}
                  >
                    <Text style={localStyles.clearUnitLabel}>Clear unit</Text>
                  </Pressable>
                )}
              </View>
            ) : null}
          </View>
        </>
      ) : null}
      {addError ? <Text style={styles.error}>{addError}</Text> : null}
      {showDuplicatePrompt ? (
        <View style={localStyles.duplicatePrompt}>
          <Text style={localStyles.duplicatePromptTitle}>{duplicateTitle}</Text>
          {duplicateSubtitle ? (
            <Text style={localStyles.duplicatePromptSubtitle}>{duplicateSubtitle}</Text>
          ) : null}
        </View>
      ) : null}
      <View style={styles.sheetActions}>
        {showDuplicatePrompt ? (
          <>
            <AppButton
              title={duplicatePrimaryActionLabel}
              onPress={onDuplicatePrimaryAction ?? onAddItem}
              disabled={nameValue.trim().length === 0}
              fullWidth
              accentKey="shopping"
            />
            <AppButton
              title={duplicateSecondaryActionLabel}
              onPress={onAddAsNew ?? onAddItem}
              disabled={nameValue.trim().length === 0}
              fullWidth
              variant="ghost"
            />
          </>
        ) : (
          <AppButton
            title={addItemTitle}
            onPress={onAddItem}
            disabled={nameValue.trim().length === 0}
            fullWidth
            accentKey="shopping"
          />
        )}
      </View>
    </>
  );
}

const localStyles = StyleSheet.create({
  detailsToggle: {
    minHeight: 36,
    alignSelf: 'flex-start',
    justifyContent: 'center',
  },
  detailsTogglePressed: {
    opacity: 0.65,
  },
  detailsToggleLabel: {
    fontSize: 14,
    fontWeight: '600',
    color: '#5b6f95',
  },
  detailsPanel: {
    gap: 6,
  },
  amountLabel: {
    fontSize: textStyles.subtle.fontSize,
    color: theme.colors.textSecondary,
    fontFamily: theme.typography.body,
    textTransform: 'uppercase',
    letterSpacing: 0.8,
  },
  unitSection: {
    gap: 6,
  },
  moreUnitsToggle: {
    minHeight: 36,
    paddingHorizontal: 12,
    borderRadius: 18,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#eef2f7',
    borderWidth: 1,
    borderColor: '#d8dde6',
  },
  moreUnitsTogglePressed: {
    opacity: 0.8,
  },
  moreUnitsToggleLabel: {
    fontSize: 13,
    fontWeight: '600',
    color: '#5b6f95',
  },
  clearUnitRow: {
    minHeight: 28,
    alignSelf: 'flex-start',
    justifyContent: 'center',
  },
  clearUnitRowPressed: {
    opacity: 0.65,
  },
  clearUnitLabel: {
    fontSize: 13,
    fontWeight: '600',
    color: '#6b7280',
  },
  successFeedbackRow: {
    minHeight: 34,
    borderRadius: 12,
    paddingHorizontal: 12,
    paddingVertical: 8,
    justifyContent: 'center',
    backgroundColor: 'rgba(47,122,79,0.12)',
    borderWidth: 1,
    borderColor: 'rgba(47,122,79,0.22)',
  },
  successFeedbackText: {
    fontSize: 13,
    fontWeight: '600',
    color: theme.colors.success,
    fontFamily: theme.typography.body,
  },
  duplicatePrompt: {
    gap: 4,
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: theme.spacing.sm,
    backgroundColor: theme.colors.surfaceAlt,
  },
  duplicatePromptTitle: {
    ...textStyles.body,
    fontWeight: '600',
  },
  duplicatePromptSubtitle: {
    ...textStyles.subtle,
  },
});
