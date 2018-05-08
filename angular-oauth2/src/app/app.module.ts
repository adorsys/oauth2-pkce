import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {AppComponent} from './app.component';
import {LoginComponent} from './login/login.component';
import {UserService} from "./user/user.service";
import {HttpClientModule} from "@angular/common/http";
import {RouterModule, Routes} from "@angular/router";
import {TokenComponent} from './token/token.component';
import {CodeProviderService} from "./auth/code-provider.service";
import {TokenService} from "./auth/token.service";
import {LoginResponseParserService} from "./auth/login-response-parser.service";
import {AuthenticationService} from "./auth/authentication.service";
import {Base64} from "./util/base64.service";

const routes: Routes = [
  {path: '', redirectTo: 'login', pathMatch: 'full'},
  {path: 'token', component: TokenComponent},
  {path: 'login', component: LoginComponent},
  {path: '**', redirectTo: 'login'}
];

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    TokenComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    RouterModule.forRoot(routes, {useHash: false})
  ],
  providers: [
    UserService,
    LoginResponseParserService,
    CodeProviderService,
    TokenService,
    AuthenticationService,
    Base64
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
