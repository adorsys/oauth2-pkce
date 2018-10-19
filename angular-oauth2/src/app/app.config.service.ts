import { Injectable } from '@angular/core';
import {AppConfig} from "./env/app-config";

@Injectable({
  providedIn: 'root'
})
export class AppConfigService {

  constructor(private appConfig: AppConfig) { }

  public getBackendUrl(): string {
    return this.appConfig.getValue("NG_BACKEND_URL");
  }

  public getLoginEndpoint(): string {
    return this.appConfig.getValue("NG_LOGIN_ENDPOINT");
  }

  public getLogoutEndpoint(): string {
    return this.appConfig.getValue("NG_LOGOUT_ENDPOINT");
  }

  public getRedirectUri(): string {
    return this.appConfig.getValue("NG_REDIRECT_URI");
  }
}
