export class AuthCode {

  private code: string;

  public constructor(code?: string) {
    if(code) {
      this.code = code;
    }
  }

  public get value(): string {
    return this.code;
  }

  public get isEmpty(): boolean {
    return this.code == null || this.code.length == 0;
  }
}
