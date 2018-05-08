import {Component, OnInit} from '@angular/core';
import {CodeProviderService} from "../auth/code-provider.service";
import {AuthenticationService} from "../auth/authentication.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-token',
  templateUrl: './token.component.html',
  styleUrls: ['./token.component.css']
})
export class TokenComponent implements OnInit {

  constructor(
    private codeProvider: CodeProviderService,
    private authenticationService: AuthenticationService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.codeProvider.code.subscribe(
      code => {
        if(!code.isEmpty) {
          this.authenticationService.exchangeToken(code.value).subscribe(
            () => {
              this.router.navigate(['/login']);
            }
          );
        }
      }
    );
  }
}
