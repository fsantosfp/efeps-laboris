const NY_TIMEZONE = 'America/New_York';

/**
 * Converte um numero de horas decimais (ex: 7.83) para um formato de string "H:MMh" (ex: 7:50h).
 * @param {number} decimalHours O número de horas em formato decimal.
 * @return {string} A string formatada
 */
export const formatDecimalHours = (decimalHours) => {

    if(decimalHours == null || isNaN(decimalHours)) return "0:00h";

    const hours = Math.floor(decimalHours);
    const decimalPart = decimalHours - hours;
    let minutes = Math.round(decimalPart * 60);

    if(minutes === 60){
        hours += 1;
        minutes = 0;
    }

    const paddedMinutes = String(minutes).padStart(2, '0');

    return `${hours}:${paddedMinutes} h`;
}

/**
 * Formata uma string ISO como data no padrão americano MM/DD/YYYY em horário de New York.
 * @param {string} isoString
 * @return {string}
 */
export const formatDateET = (isoString) => {
    if (!isoString) return '';
    return new Date(isoString).toLocaleDateString('en-US', {
        timeZone: NY_TIMEZONE,
        month: '2-digit',
        day: '2-digit',
        year: 'numeric',
    });
};

/**
 * Formata uma string ISO como hora no padrão 12h (h:MM AM/PM) em horário de New York.
 * @param {string} isoString
 * @return {string}
 */
export const formatTimeET = (isoString) => {
    if (!isoString) return '';
    return new Date(isoString).toLocaleTimeString('en-US', {
        timeZone: NY_TIMEZONE,
        hour: '2-digit',
        minute: '2-digit',
        hour12: true,
    });
};

/**
 * Formata um número como valor monetário em dólares americanos (ex: $1,234.56).
 * @param {number} value
 * @return {string}
 */
export const formatCurrencyUS = (value) => {
    if (value == null || isNaN(value)) return '$0.00';
    return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD',
    }).format(value);
};

/**
 * Retorna o offset de UTC para America/New_York (em horas inteiras) para um dado Instant,
 * verificando o horário NY exibido às 04:30 UTC do mesmo dia.
 * Funciona para EDT (UTC-4) e EST (UTC-5), incluindo dias de transição DST.
 * @param {number} utcMs - ms desde epoch de referência
 * @returns {number} offset em horas (4 para EDT, 5 para EST)
 */
function getNYOffsetHours(utcMs) {
    const ref = new Date(utcMs);
    const nyHour = parseInt(
        ref.toLocaleString('en-US', {
            timeZone: NY_TIMEZONE,
            hour: '2-digit',
            hour12: false,
        })
    );
    // Se NY mostrar 00 (meia-noite) está em EDT (04:30 UTC - 4h = 00:30 NY)
    // Se NY mostrar 23 (11 PM do dia anterior) está em EST (04:30 UTC - 5h = 23:30 NY)
    return nyHour === 0 ? 4 : 5;
}

/**
 * Converte uma string de data "YYYY-MM-DD" para o início do dia (00:00:00) em horário de New York,
 * retornando uma string ISO UTC para envio ao backend.
 * Lida corretamente com EDT (UTC-4) e EST (UTC-5) incluindo dias de transição DST.
 * @param {string} dateStr "YYYY-MM-DD"
 * @return {string} ISO string em UTC
 */
export const toETDayStart = (dateStr) => {
    if (!dateStr) return '';
    const [y, m, d] = dateStr.split('-').map(Number);
    // Usa 04:30 UTC como referência para detectar o offset correto (sempre perto de meia-noite ET)
    const refMs = Date.UTC(y, m - 1, d, 4, 30, 0);
    const offsetHours = getNYOffsetHours(refMs);
    return new Date(Date.UTC(y, m - 1, d, offsetHours, 0, 0, 0)).toISOString();
};

/**
 * Converte uma string de data "YYYY-MM-DD" para o fim do dia (23:59:59.999) em horário de New York,
 * retornando uma string ISO UTC para envio ao backend.
 * @param {string} dateStr "YYYY-MM-DD"
 * @return {string} ISO string em UTC
 */
export const toETDayEnd = (dateStr) => {
    if (!dateStr) return '';
    const [y, m, d] = dateStr.split('-').map(Number);
    // Usa 04:30 UTC do dia SEGUINTE para detectar o offset do fim do dia (23:59 ET)
    const refMs = Date.UTC(y, m - 1, d + 1, 4, 30, 0);
    const offsetHours = getNYOffsetHours(refMs);
    // 23:59:59.999 ET = começo do dia seguinte em UTC - 1ms
    return new Date(Date.UTC(y, m - 1, d + 1, offsetHours, 0, 0, 0) - 1).toISOString();
};


/**
 * Retorna o offset de America/New_York em minutos para uma determinada data local,
 * levando em conta horário de verão.
 * @param {Date} date
 * @return {number} Offset em minutos (ex: -240 para EDT, -300 para EST)
 */
function getNYOffsetMinutes(date) {
    const utcString = date.toLocaleString('en-US', { timeZone: 'UTC' });
    const nyString = date.toLocaleString('en-US', { timeZone: NY_TIMEZONE });
    const utcDate = new Date(utcString);
    const nyDate = new Date(nyString);
    return (nyDate - utcDate) / 60000;
}