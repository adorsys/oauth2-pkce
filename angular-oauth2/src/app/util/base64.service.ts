import { Injectable } from '@angular/core';

@Injectable()
export class Base64 {

  constructor() { }

  public encode(text: string): string {
    return btoa(text);
  }

  public decode(base64: string): string {
    return atob(base64);
  }
}
