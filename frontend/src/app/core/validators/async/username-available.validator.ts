import { AbstractControl, AsyncValidatorFn, ValidationErrors } from '@angular/forms';
import { Observable, of, timer } from 'rxjs';
import { map, switchMap, catchError } from 'rxjs/operators';

/**
 * Interfaz del servicio para verificar disponibilidad de username.
 * Permite inyectar el servicio real via dependency injection.
 */
export interface UsernameCheckService {
  checkUsernameExists(username: string): Observable<boolean>;
}

/**
 * Configuracion para el validador de username
 */
export interface UsernameValidatorConfig {
  /** Tiempo de debounce en milisegundos (default: 500) */
  debounceMs?: number;
  /** Longitud minima del username para validar (default: 3) */
  minLength?: number;
  /** Permitir caracteres especiales ademas de letras, numeros, guiones y guiones bajos */
  allowSpecialChars?: boolean;
}

const DEFAULT_CONFIG: Required<UsernameValidatorConfig> = {
  debounceMs: 500,
  minLength: 3,
  allowSpecialChars: false
};

/**
 * Expresion regular para username valido:
 * - Solo letras, numeros, guiones (-) y guiones bajos (_)
 * - Debe comenzar con letra o numero
 * - Entre 3 y 30 caracteres
 */
const USERNAME_REGEX = /^[a-zA-Z0-9][a-zA-Z0-9_-]{2,29}$/;

/**
 * Valida el formato basico del username antes de hacer la llamada async
 */
function isValidUsernameFormat(username: string, allowSpecialChars: boolean): boolean {
  if (allowSpecialChars) {
    // Permitir mas caracteres pero mantener restricciones basicas
    return username.length >= 3 && username.length <= 30;
  }
  return USERNAME_REGEX.test(username);
}

/**
 * Crea un validador asincrono que verifica si un username esta disponible.
 * Esta version requiere inyectar un UsernameCheckService.
 *
 * @param usernameCheckService - Servicio que implementa checkUsernameExists
 * @param config - Configuracion opcional del validador
 * @returns AsyncValidatorFn
 *
 * @example
 * ```typescript
 * // En un componente
 * private userService = inject(UserService);
 *
 * this.form = this.fb.group({
 *   username: ['', {
 *     validators: [Validators.required, Validators.minLength(3)],
 *     asyncValidators: [createUsernameAvailableValidator(this.userService)],
 *     updateOn: 'blur'
 *   }]
 * });
 * ```
 */
export function createUsernameAvailableValidator(
  usernameCheckService: UsernameCheckService,
  config: UsernameValidatorConfig = {}
): AsyncValidatorFn {
  const { debounceMs, minLength, allowSpecialChars } = { ...DEFAULT_CONFIG, ...config };

  return (control: AbstractControl): Observable<ValidationErrors | null> => {
    const username = control.value as string;

    // No validar si esta vacio o es muy corto
    if (!username || username.length < minLength) {
      return of(null);
    }

    // No validar si el formato es invalido
    if (!isValidUsernameFormat(username, allowSpecialChars)) {
      return of(null);
    }

    return timer(debounceMs).pipe(
      switchMap(() => usernameCheckService.checkUsernameExists(username)),
      map(exists => (exists ? { usernameTaken: true } : null)),
      catchError(() => of(null)) // En caso de error, no bloquear el formulario
    );
  };
}

/**
 * Validador asincrono simulado para demostracion y testing.
 * Simula una llamada API con delay configurable y usa una lista
 * de usernames "ocupados" para pruebas.
 *
 * Usernames ocupados simulados: admin, root, user, test, demo, moderator, support
 *
 * @param config - Configuracion opcional del validador
 * @returns AsyncValidatorFn
 *
 * @example
 * ```typescript
 * // En un componente (para demostracion/testing)
 * this.form = this.fb.group({
 *   username: ['', {
 *     validators: [Validators.required, Validators.minLength(3)],
 *     asyncValidators: [usernameAvailableValidator()],
 *     updateOn: 'blur'
 *   }]
 * });
 * ```
 */
export function usernameAvailableValidator(
  config: UsernameValidatorConfig = {}
): AsyncValidatorFn {
  const { debounceMs, minLength, allowSpecialChars } = { ...DEFAULT_CONFIG, ...config };

  // Lista simulada de usernames ocupados
  const takenUsernames = [
    'admin',
    'root',
    'user',
    'test',
    'demo',
    'moderator',
    'support',
    'administrator',
    'superuser',
    'guest'
  ];

  return (control: AbstractControl): Observable<ValidationErrors | null> => {
    const username = control.value as string;

    // No validar si esta vacio o es muy corto
    if (!username || username.length < minLength) {
      return of(null);
    }

    // No validar si el formato es invalido
    if (!isValidUsernameFormat(username, allowSpecialChars)) {
      return of(null);
    }

    // Simular llamada API con delay
    return timer(debounceMs).pipe(
      map(() => {
        const usernameLower = username.toLowerCase().trim();
        const isTaken = takenUsernames.includes(usernameLower);
        return isTaken ? { usernameTaken: true } : null;
      }),
      catchError(() => of(null))
    );
  };
}

/**
 * Obtiene el mensaje de error para username ocupado.
 *
 * @returns String con el mensaje de error
 */
export function getUsernameTakenError(): string {
  return 'Este nombre de usuario ya esta en uso';
}

/**
 * Obtiene el mensaje de error para formato de username invalido.
 *
 * @returns String con el mensaje de error
 */
export function getUsernameFormatError(): string {
  return 'El username solo puede contener letras, numeros, guiones y guiones bajos';
}

/**
 * Validador sincrono para formato de username.
 * Usar junto con el validador async para una validacion completa.
 *
 * @example
 * ```typescript
 * this.form = this.fb.group({
 *   username: ['', {
 *     validators: [Validators.required, usernameFormatValidator()],
 *     asyncValidators: [usernameAvailableValidator()],
 *     updateOn: 'blur'
 *   }]
 * });
 * ```
 */
export function usernameFormatValidator(): (control: AbstractControl) => ValidationErrors | null {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value as string;

    if (!value) {
      return null;
    }

    if (!USERNAME_REGEX.test(value)) {
      return {
        usernameFormat: {
          message: getUsernameFormatError()
        }
      };
    }

    return null;
  };
}
