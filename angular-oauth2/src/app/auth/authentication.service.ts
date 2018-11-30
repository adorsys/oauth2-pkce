import {Injectable} from '@angular/core';
import {Router} from "@angular/router";
import {AppConfigService} from "../app.config.service";
import {AuthenticatedUser} from "./authenticated-user";
import {UserService} from "../user/user.service";
import {TokenService} from "./token-service";

@Injectable()
export class AuthenticationService {
  private _authenticatedUser: AuthenticatedUser = null;

  constructor(private appConfigService: AppConfigService,
              private userService: UserService,
              private tokenService: TokenService,
              private router: Router) {
  }

  public get isAuthenticated(): Promise<AuthenticatedUser> {
    if (this._authenticatedUser == null) {
      return this.loadUser().catch(this.handleError);
    } else {
      return new Promise((resolve) => {
        resolve(this._authenticatedUser);
      });
    }
  }

  private loadUser(): Promise<AuthenticatedUser> {
    let maybeUser = this.userService.getUser();

    return maybeUser.map(u => {
      return {
        isAuthenticated: true,
        user: u
      }
    }).toPromise();
  }

  private handleError(err): Promise<AuthenticatedUser> {
    return new Promise((resolve) => {
      resolve({
        isAuthenticated: false
      });
    });
  }

  public login(): void {
    const loginUrl = `${this.appConfigService.getBackendUrl()}${this.appConfigService.getLoginEndpoint()}`;
    this.router.navigate([`${loginUrl}`]);
  }
}
