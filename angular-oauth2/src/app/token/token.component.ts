import {Component, OnInit} from '@angular/core';
import {AuthenticationService} from "../auth/authentication.service";
import {ActivatedRoute, Params, Router} from "@angular/router";

@Component({
  selector: 'app-token',
  templateUrl: './token.component.html',
  styleUrls: ['./token.component.css']
})
export class TokenComponent implements OnInit {

  constructor(
    private authenticationService: AuthenticationService,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    console.log("token component");

    this.route.queryParams.subscribe((params: Params) => {
      console.log(`got params: ${JSON.stringify(params)}`);

      let code = params['code'];
      console.log(`got code: ${code}`);

      this.authenticationService.exchangeToken(code).subscribe(
        () => {
          this.router.navigate(['/login']);
        }
      );
    });
  }
}
