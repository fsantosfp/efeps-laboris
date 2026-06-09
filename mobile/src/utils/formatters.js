/**
 * Converte um numero de horas decimais (ex: 7.83) para um formato de string "H:MMh" (ex: 7:50h).
 * @param {number} decimalHours O número de horas em formato decimal.
 * @return {string} A string formatada
 */

export const formatDecimalHours = (decimalHours) => {
    
    if(decimalHours == null || isNaN(decimalHours)) return "0:00h";

    let hours = Math.floor(decimalHours);
    const decimalPart = decimalHours - hours;
    let minutes = Math.round(decimalPart * 60);

    if(minutes === 60){
        hours += 1;
        minutes = 0;
    }

    const paddedMinutes = String(minutes).padStart(2, '0');

    return `${hours}:${paddedMinutes} h`;
}