import {Component, OnInit} from '@angular/core';
import {UserService} from "../user/user.service";
import {User} from "../user/user";
import {environment} from "../../environments/environment";
import {AuthenticationService} from "../auth/authentication.service";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  public user: User;

  constructor(
    private userService: UserService,
    private authenticationService: AuthenticationService
  ) {
  }

  ngOnInit() {
    if(this.authenticationService.isAuthenticated) {
      this.userService.getUser().subscribe(
        user => {
          this.user = user;
          console.log(user)
        }
      );
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

  public logout() {
    this.authenticationService.logout();
  }

  public get loginUrl() {
    return `${environment.backendUrl}${environment.loginEndpoint}`;
  }

  public login(): void {
    console.log("do login");
    this.authenticationService.login();
  }
}
