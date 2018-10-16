import {User} from "../user/user";

export interface AuthenticatedUser {
  isAuthenticated: boolean;
  user?: User
}
