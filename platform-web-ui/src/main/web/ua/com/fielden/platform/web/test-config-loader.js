window.TG_APP = await loadConfig(); 

async function loadConfig() {
    try{
        const response = await fetch('/app/configuration');
        return response.json();
    } catch (e) {
        console.error(e);
    }
} 