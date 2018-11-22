import {Injectable} from '@angular/core';
import {Observable} from "rxjs/Observable";
import {Router} from "@angular/router";
import {AppConfigService} from "../app.config.service";
import {AuthenticatedUser} from "./authenticated-user";
import {UserService} from "../user/user.service";
import {catchError} from "rxjs/internal/operators";
import {TokenService} from "./token-service";

@Injectable()
export class AuthenticationService {
  private _authenticatedUser: AuthenticatedUser = null;

  constructor(private appConfigService: AppConfigService,
              private userService: UserService,
              private tokenService: TokenService,
              private router: Router) {
  }

  public get isAuthenticated(): Observable<AuthenticatedUser> {
    if (this._authenticatedUser == null) {
      return this.loadUser().pipe(
        catchError(this.handleError)
      );
    } else {
      Observable.create(this._authenticatedUser)
    }
  }

  private loadUser(): Observable<AuthenticatedUser> {
    let maybeUser = this.userService.getUser();

    return maybeUser.map(u => {
      return {
        isAuthenticated: true,
        user: u
      }
    });
  }

  private handleError(err): Observable<AuthenticatedUser> {
    return new Observable((observer) => {
      observer.next({
        isAuthenticated: false
      });
      observer.complete();
    });
  }

  public login(): void {
    const loginUrl = `${this.appConfigService.getBackendUrl()}${this.appConfigService.getLoginEndpoint()}`;
    this.router.navigate([`${loginUrl}`]);
  }

  public exchangeToken(code: string): Observable<AuthenticatedUser> {
    return this.tokenService.getToken(code)
      .pipe(
        () => {
          return this.loadUser().map(u => {
            this._authenticatedUser = u;
            return u;
          });
        }
      );
  }
}
