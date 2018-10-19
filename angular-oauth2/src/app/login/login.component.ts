import {Component, OnInit} from '@angular/core';
import {User} from "../user/user";
import {AuthenticationService} from "../auth/authentication.service";
import {AppConfigService} from "../app.config.service";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  public user: User;
  private _isAuthenticated = false;

  constructor(
    private appConfigService: AppConfigService,
    private authenticationService: AuthenticationService
  ) {
  }

  ngOnInit() {
    this.authenticationService.isAuthenticated.subscribe(a => {
      if(a.isAuthenticated) {
        this._isAuthenticated = a.isAuthenticated;
        this.user = a.user;
      } else {
        this._isAuthenticated = false;
      }
    });
  }

  public get authenticated() {
    return this._isAuthenticated;
  }

  public get unauthenticated() {
    return !(this._isAuthenticated);
  }

  public get loginUrl() {
    return `${this.appConfigService.getBackendUrl()}${this.appConfigService.getLoginEndpoint()}`;
  }

  public get loginUrlWithRedirect() {
    return `${this.appConfigService.getBackendUrl()}${this.appConfigService.getLoginEndpoint()}?redirect_uri=${this.appConfigService.getRedirectUri()}`;
  }

  public get logoutUrl() {
    return `${this.appConfigService.getBackendUrl()}${this.appConfigService.getLogoutEndpoint()}`;
  }

  public get logoutUrlWithRedirect() {
    return `${this.appConfigService.getBackendUrl()}${this.appConfigService.getLogoutEndpoint()}?redirect_uri=${this.appConfigService.getRedirectUri()}`;
  }
}
