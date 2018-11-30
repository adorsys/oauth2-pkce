import {Component, OnInit} from '@angular/core';
import {User} from "../user/user";
import {AuthenticationService} from "../auth/authentication.service";
import {AppConfigService} from "../app.config.service";
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  public user: User;
  private _isAuthenticated = false;
  private _code;

  constructor(
    private appConfigService: AppConfigService,
    private authenticationService: AuthenticationService,
    private route: ActivatedRoute
  ) {
  }

  ngOnInit() {
    this.authenticationService.isAuthenticated.then(a => {
      if(a.isAuthenticated) {
        this._isAuthenticated = a.isAuthenticated;
        this.user = a.user;
      } else {
        this._isAuthenticated = false;

        this.readCode();
      }
    });
  }

  private readCode() {
    this._code = this.route.snapshot.queryParamMap.get('code');
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

  public get getTokenForCodeUrl() {
    return `${this.appConfigService.getBackendUrl()}${this.appConfigService.getTokenEndpoint()}?code=${this._code}&redirect_uri=${this.appConfigService.getRedirectUri()}`;
  }

  public get code(): string {
    return this._code;
  }
}
