import { expect, test } from '@playwright/test'

test.describe('public portal', () => {
  test('landing page exposes discovery and staff sign-in', async ({ page }) => {
    await page.goto('/')
    await expect(page.getByRole('heading', { name: /discover trusted digital resources/i })).toBeVisible()
    await expect(page.getByRole('link', { name: /staff sign in/i })).toBeVisible()
    await expect(page.getByRole('link', { name: /browse catalogue/i })).toBeVisible()
  })

  test('search page renders filters and an empty or populated result area', async ({ page }) => {
    await page.goto('/search')
    await expect(page.getByRole('heading', { name: /catalogue search/i })).toBeVisible()
    await expect(page.getByRole('heading', { name: /filters/i })).toBeVisible()
    await expect(page.getByPlaceholder(/title, author, subject/i)).toBeVisible()
  })

  test('staff routes require authentication', async ({ page }) => {
    await page.goto('/staff')
    await expect(page).toHaveURL(/\/login$/)
    await expect(page.getByRole('heading', { name: /staff sign in/i })).toBeVisible()
  })
})
