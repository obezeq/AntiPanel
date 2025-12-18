import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Interfaz para errores del validador NIF
 */
export interface NifValidationError {
  message: string;
  invalidFormat?: boolean;
  invalidLetter?: boolean;
  expectedLetter?: string;
  actualLetter?: string;
}

/**
 * Tabla de letras de verificacion del NIF
 * La letra se calcula dividiendo los 8 digitos entre 23 y usando el resto como indice
 */
const NIF_LETTERS = 'TRWAGMYFPDXBNJZSQVHLCKE';

/**
 * Expresion regular para validar formato NIF: 8 digitos + 1 letra
 */
const NIF_REGEX = /^[0-9]{8}[A-Z]$/i;

/**
 * Expresion regular para validar formato NIE: X/Y/Z + 7 digitos + 1 letra
 */
const NIE_REGEX = /^[XYZ][0-9]{7}[A-Z]$/i;

/**
 * Calcula la letra de verificacion para un numero de NIF/NIE
 * @param number - El numero (8 digitos para NIF, 7 para NIE con prefijo convertido)
 * @returns La letra de verificacion esperada
 */
function calculateNifLetter(number: number): string {
  return NIF_LETTERS[number % 23];
}

/**
 * Convierte el prefijo de NIE a numero equivalente
 * X = 0, Y = 1, Z = 2
 */
function convertNiePrefix(prefix: string): string {
  const map: Record<string, string> = { X: '0', Y: '1', Z: '2' };
  return map[prefix.toUpperCase()] || prefix;
}

/**
 * Validador de NIF/NIE espanol
 *
 * Valida:
 * - NIF: 8 digitos + 1 letra de verificacion (ej: 12345678Z)
 * - NIE: X/Y/Z + 7 digitos + 1 letra de verificacion (ej: X1234567L)
 *
 * El algoritmo de verificacion:
 * 1. Para NIF: Se divide el numero de 8 digitos entre 23
 * 2. El resto indica la posicion de la letra en la tabla TRWAGMYFPDXBNJZSQVHLCKE
 * 3. Para NIE: X se sustituye por 0, Y por 1, Z por 2, y se aplica el mismo algoritmo
 *
 * @example
 * ```typescript
 * // En un formulario reactivo:
 * this.form = this.fb.group({
 *   nif: ['', [Validators.required, nifValidator()]]
 * });
 *
 * // En template:
 * @if (form.get('nif')?.errors?.['nif']) {
 *   <span class="error">{{ form.get('nif')?.errors?.['nif'].message }}</span>
 * }
 * ```
 */
export function nifValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value?.trim().toUpperCase();

    // Si no hay valor, no validamos (usar Validators.required si es obligatorio)
    if (!value) {
      return null;
    }

    // Verificar si es formato NIF o NIE
    const isNif = NIF_REGEX.test(value);
    const isNie = NIE_REGEX.test(value);

    if (!isNif && !isNie) {
      const error: NifValidationError = {
        message: 'Formato invalido. Use 8 digitos + letra (NIF) o X/Y/Z + 7 digitos + letra (NIE)',
        invalidFormat: true
      };
      return { nif: error };
    }

    // Extraer numero y letra
    let numberStr: string;
    let actualLetter: string;

    if (isNie) {
      // NIE: Convertir prefijo X/Y/Z a numero
      const prefix = value[0];
      numberStr = convertNiePrefix(prefix) + value.slice(1, 8);
      actualLetter = value[8];
    } else {
      // NIF: 8 primeros caracteres son el numero
      numberStr = value.slice(0, 8);
      actualLetter = value[8];
    }

    const number = parseInt(numberStr, 10);
    const expectedLetter = calculateNifLetter(number);

    if (expectedLetter !== actualLetter) {
      const error: NifValidationError = {
        message: `Letra de verificacion incorrecta. Esperada: ${expectedLetter}`,
        invalidLetter: true,
        expectedLetter,
        actualLetter
      };
      return { nif: error };
    }

    return null;
  };
}

/**
 * Obtiene un mensaje de error legible para errores de NIF
 * @param errors - Los errores del validador
 * @returns Mensaje de error formateado o null
 */
export function getNifError(errors: ValidationErrors | null): string | null {
  if (!errors?.['nif']) {
    return null;
  }
  return errors['nif'].message;
}

/**
 * Valida un NIF/NIE sin usar FormControl (utilidad standalone)
 * @param value - El valor a validar
 * @returns true si es valido, false si no
 */
export function isValidNif(value: string): boolean {
  const mockControl = { value } as AbstractControl;
  return nifValidator()(mockControl) === null;
}
