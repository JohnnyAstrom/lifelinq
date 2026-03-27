// @ts-nocheck
import { parseSharedRecipeAsset, parseSharedRecipeUrl } from './deepLinkIntents';

describe('deep link recipe share parsing', () => {
  test('parses shared recipe import URL payloads', () => {
    expect(
      parseSharedRecipeUrl(
        'mobileapp://recipes/import-url?url=https%3A%2F%2Fexample.com%2Frecipe&handoff=1'
      )
    ).toEqual({
      url: 'https://example.com/recipe',
      invalid: false,
    });
  });

  test('parses invalid shared recipe handoff payloads calmly', () => {
    expect(
      parseSharedRecipeUrl('mobileapp://recipes/import-url?error=invalid&handoff=2')
    ).toEqual({
      url: null,
      invalid: true,
    });
  });

  test('ignores unrelated deep links', () => {
    expect(parseSharedRecipeUrl('mobileapp://invite?token=abc')).toBeNull();
  });
});

describe('deep link recipe asset share parsing', () => {
  test('parses shared document handoff payloads', () => {
    expect(
      parseSharedRecipeAsset(
        'mobileapp://recipes/import-asset?assetKind=DOCUMENT&referenceId=content%3A%2F%2Fmedia%2F1&originalFilename=recipe.pdf&mimeType=application%2Fpdf&handoff=3'
      )
    ).toEqual({
      assetKind: 'DOCUMENT',
      referenceId: 'content://media/1',
      sourceLabel: null,
      originalFilename: 'recipe.pdf',
      mimeType: 'application/pdf',
      invalid: false,
      unsupported: false,
    });
  });

  test('parses shared image handoff payloads', () => {
    expect(
      parseSharedRecipeAsset(
        'mobileapp://recipes/import-asset?assetKind=IMAGE&referenceId=content%3A%2F%2Fmedia%2F2&sourceLabel=Shared%20image&mimeType=image%2Fjpeg&handoff=4'
      )
    ).toEqual({
      assetKind: 'IMAGE',
      referenceId: 'content://media/2',
      sourceLabel: 'Shared image',
      originalFilename: null,
      mimeType: 'image/jpeg',
      invalid: false,
      unsupported: false,
    });
  });

  test('parses unsupported shared asset payloads calmly', () => {
    expect(
      parseSharedRecipeAsset('mobileapp://recipes/import-asset?error=unsupported&handoff=5')
    ).toEqual({
      assetKind: 'DOCUMENT',
      referenceId: null,
      sourceLabel: null,
      originalFilename: null,
      mimeType: null,
      invalid: false,
      unsupported: true,
    });
  });
});
