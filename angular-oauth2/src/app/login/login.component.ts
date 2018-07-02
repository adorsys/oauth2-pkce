import {Component, OnInit} from '@angular/core';
import {UserService} from "../user/user.service";
import {User} from "../user/user";
import {environment} from "../../environments/environment";
import {AuthenticationService} from "../auth/authentication.service";
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  public user: User;

  constructor(
    private userService: UserService,
    private authenticationService: AuthenticationService,
    private route: ActivatedRoute
  ) {
  }

  ngOnInit() {
    console.log("login component");

    if(this.authenticationService.isAuthenticated) {
      this.userService.getUser().subscribe(
        user => {
          this.user = user;
          console.log(user)
        }
      );
    } else {
      let code = this.route.queryParams['code'];
      if(code) {
        this.authenticationService.exchangeToken(code.value);
      }
    }
  }

  public get backendUrl() {
    return environment.backendUrl;
  }

  public get authenticated() {
    return this.authenticationService.isAuthenticated;
  }

  public get unauthenticated() {
    return !(this.authenticationService.isAuthenticated);
  }

  public get loginUrl() {
    return `${environment.backendUrl}${environment.loginEndpoint}?redirect_uri=${environment.redirectUri}`;
  }

  public get logoutUrl() {
    return `${environment.backendUrl}${environment.logoutEndpoint}`;
  }

  public login(): void {
    console.log("do login");
    this.authenticationService.login();
  }
}
