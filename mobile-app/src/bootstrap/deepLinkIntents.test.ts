// @ts-nocheck
import { parseSharedRecipeUrl } from './deepLinkIntents';

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
