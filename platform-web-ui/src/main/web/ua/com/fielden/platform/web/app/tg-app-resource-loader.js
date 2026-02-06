const APPLICATION_CONFIGURATION_RESOURCE = '/app/configuration';
const APPLICATION_CONFIGURATION_KEY = 'app_configuration';
const DEFAULT_MAX_RETRIES = 3;
const DEFAULT_BASE_DELAY = 500;

/**
 * Loads application resources and enhances the DOM with required elements.
 *
 * @param {string} resourcesToLoad - URI of the main application resource to load
 */
export async function loadAppResources (resourcesToLoad) {
    try {
        window.TG_APP = await tryLoadingConfiguration();
        setTitle();
        setMainPanelColour();
        await tryLoadingAppResources(resourcesToLoad);
        addAppTemplate();
        addQrCodeScanner();
    } catch (err) {
        showMessage(`${err.message.endsWith(".") ? err.message : (err.message + ".")}\nRefresh to try again.`);
    }
}

/**
 * Loads application configuration, falling back to localStorage on network failure.
 * Marks configuration as stale and adds an `errorMsg` property explaining the fallback reason.
 *
 * @param {Object} delayConfig - Retry configuration with `maxAttempts` and `baseDelay` properties
 * @returns {Object} Application configuration object (fresh or stale from localStorage)
 */
async function tryLoadingConfiguration(delayConfig) {
    try {
        const res = await fetchConfiguration(delayConfig);
        if (!res.ok) {
            if (res.status == 500) {
                const error = await res.json();
                throw new Error(`${error.errorMsg || error.message || "Connection issue."}`);
            } else {
                throw new Error(`Connection issue.`);
            }
        }
        const appConfig = await res.json();
        localStorage.setItem(APPLICATION_CONFIGURATION_KEY, JSON.stringify(appConfig));
        return appConfig;
    } catch (err) {
        const appConfigValue = localStorage.getItem(APPLICATION_CONFIGURATION_KEY);
        if (appConfigValue) {
            // Fallback to the saved one.
            const appConfig = JSON.parse(appConfigValue);
            appConfig.isStale = true;
            appConfig.errorMsg = `Could not load configuration: ${err.message.endsWith(".") ? err.message.toLowerCase() : (err.message.toLowerCase() + ".")}\nUsing saved configuration instead.`;
            return appConfig;
        } else {
            // Otherwise throw exception indicating that configuration could not be loaded.
            throw new Error (`Could not load configuration: ${err.message.toLowerCase()}`);
        }
    }
}

/**
 * Fetches application configuration with automatic retry on transient HTTP failures.
 * Retries on status codes 408, 429, 500, 502, 503, 504 using exponential backoff.
 * Returns response immediately on success, non-retryable status, or final attempt.
 * Throws on final failure after all retries.
 *
 * @param {Object} [retryConfig] - Retry configuration
 * @param {number} [retryConfig.maxRetries=DEFAULT_MAX_RETRIES] - Maximum retry attempts
 * @param {number} [retryConfig.baseDelay=DEFAULT_BASE_DELAY] - Base delay in ms for exponential backoff
 * @returns {Response} Fetch response (successful or final non-retryable status)
 * @throws {Error} Last fetch error after all retries exhausted
 */
async function fetchConfiguration({ maxRetries = DEFAULT_MAX_RETRIES, baseDelay = DEFAULT_BASE_DELAY } = {}) {
    const retriables = [408, 429, 500, 502, 503, 504];
    for (let attempt = 1; attempt <= maxRetries; attempt++) {
        try {
            const response = await fetch(APPLICATION_CONFIGURATION_RESOURCE);
            if (response.ok || !retriables.includes(response.status) || attempt >= maxRetries) {
                return response;
            }
        } catch (err) {
            if (attempt >= maxRetries) {
                throw err;
            } 
        }
        await delay(baseDelay, attempt - 1);
    }
}

/**
 * Dynamically imports application resources with retry on transient network failures.
 * Uses exponential backoff for retryable `import()` errors (network/fetch failures).
 * Throws immediately on non-retryable errors or after max retries exhausted.
 *
 * @param {string} modulePath - Path/URL of the ES module to import
 * @param {Object} [retryConfig] - Retry configuration
 * @param {number} [retryConfig.maxRetries=DEFAULT_MAX_RETRIES] - Maximum retry attempts
 * @param {number} [retryConfig.baseDelay=DEFAULT_BASE_DELAY] - Base delay in ms for exponential backoff
 * @returns {Promise<ModuleNamespaceObject>} Successfully imported module namespace object
 * @throws {Error} Original import error if non-retryable or all retries fail
 */
async function tryLoadingAppResources(modulePath, { maxRetries = DEFAULT_MAX_RETRIES, baseDelay = DEFAULT_BASE_DELAY } = {}) {
    for (let attempt = 1; attempt <= maxRetries; attempt++) {
        try {
            return await import(modulePath);
        } catch (error) {
            if (attempt >= maxRetries || !isRetryableError(error)) {
                throw new Error(`Could not load all required resources.`);
            }
            await delay(baseDelay, attempt - 1);
        }
    }
}

/**
 * Determines if an error from dynamic `import()` is transient and retryable.
 * Only retries network-related `TypeError`s from failed module fetches.
 * Excludes permanent failures like syntax errors, 404s, or CORS issues.
 *
 * @param {Error} error - Error thrown by `import()` call
 * @returns {boolean} `true` if error indicates transient network failure suitable for retry
 */
function isRetryableError(error) {
  return error.name === 'TypeError' && 
         (error.message.includes('Failed to fetch') || 
          error.message.includes('network'));
}

/**
 * Calculates an exponentially increasing delay with jitter for retry backoff.
 * Uses the formula: `delay = baseDelay * 2^attempt + jitter`, where jitter adds
 * controlled randomness (±20% of calculated delay) to prevent thundering herd issues.
 *
 * @param {number} baseDelay - Base delay in milliseconds (typically 500)
 * @param {number} attempt - Current retry attempt number (0 = first retry, 1 = second, etc.)
 * @returns {Promise<void>} Resolves after the calculated delay (non-rejecting)
 */
function delay (baseDelay, attempt) {
    const delay = baseDelay * 2 ** attempt;
    const jitter = delay * (Math.random() * 0.4 - 0.2);
    return new Promise(resolve => setTimeout(resolve, delay + jitter));
}

/**
 * Displays a message on the splash screen during the loading process.
 *
 * @param {string} message - Message to display on splash screen
 */
function showMessage(message) {
    const splashText = document.getElementById("splash-text");
    if (splashText) {
        splashText.innerText = message;
    }
}

/**
 * Sets the title property in the main application where needed (i.e., title tag and meta tags).
 */
function setTitle () {
    document.title = TG_APP.title;
    const metaApplicationName = document.querySelector('meta[name="application-name"]');
    if (metaApplicationName) {
        metaApplicationName.setAttribute("content", TG_APP.title);
    }
    const metaMobileWebAppTitle = document.querySelector('meta[name="apple-mobile-web-app-title"]');
    if (metaMobileWebAppTitle) {
        metaMobileWebAppTitle.setAttribute("content", TG_APP.title);
    }
    showMessage(`Loading ${TG_APP.title}...`);
}

/**
 * Sets the color value for the CSS property controlling the main top panel's background color.
 */
function setMainPanelColour () {
    if (window.TG_APP && window.TG_APP.panelColor) {
        document.documentElement.style.setProperty('--tg-main-pannel-color', window.TG_APP.panelColor);
    }
}

/**
 * Adds the main `tg-app-template` element to the DOM.
 */
function addAppTemplate () {
    const appElement = document.createElement("tg-app-template");
    appElement.classList.add("layout", "vertical");
    appElement.setAttribute("id", "app")
    appElement.appTitle = window.TG_APP.title;
    appElement.ideaUri = window.TG_APP.ideaUri;
    const splashText = document.getElementById("splash-text");
    if (splashText) {
        splashText.after(appElement);
    } else {
        document.body.appendChild(appElement);
    }
}

/**
 * Adds the `tg-qr-code-scanner` element to the DOM.
 */
function addQrCodeScanner () {
    const qrCodeScanner = document.createElement("tg-qr-code-scanner");
    qrCodeScanner.appendChild(createVideoFeedElement());
    qrCodeScanner.setAttribute("id", "qrScanner")
    const appElement = document.getElementById("app");
    if (appElement) {
        appElement.after(qrCodeScanner);
    } else {
        document.body.appendChild(qrCodeScanner);
    }
}

/**
 * Creates a video feed element for the QR code scanner.
 *
 * @returns {HTMLElement} Video element that receives the camera stream
 */
function createVideoFeedElement () {
    const videoFeedElement = document.createElement("div");
    videoFeedElement.setAttribute("id", "tgQrScanner");
    videoFeedElement.setAttribute("slot", "scanner");
    videoFeedElement.classList.add("no-padding");
    return videoFeedElement;
}