import {Injectable} from '@angular/core';
import {Observable} from "rxjs/Observable";
import {TokenService} from "./token.service";
import "rxjs/add/operator/mergeMap";
import {environment} from "../../environments/environment";
import {Router} from "@angular/router";

@Injectable()
export class AuthenticationService {
  public code: String;
  private _isAuthenticated: boolean = false;

  constructor(
    private tokenService: TokenService,
    private router: Router
  ) { }

  public get isAuthenticated(): boolean {
    return this._isAuthenticated;
  }

  public login(): void {
    console.log(`redirect to: ${environment.backendUrl}${environment.loginEndpoint}`);
    this.router.navigate([`${environment.backendUrl}${environment.loginEndpoint}`]);
  }

  public logout(): void {
    this._isAuthenticated = false;
    console.log(`redirect to: ${environment.backendUrl}${environment.logoutEndpoint}`);
    this.router.navigate([`${environment.backendUrl}${environment.logoutEndpoint}`]);
  }

  public exchangeToken(code: string): Observable<void> {
    return this.tokenService.getToken(code).map(
      () => {
        this._isAuthenticated = true;
      }
    );
  }
}
