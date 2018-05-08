export interface TokenResponse {
  refresh_token: string,
  refresh_token_expires_in: number,
  id_token: string,
  access_token: string,
  token_type: string,
  expires_in: number
}
