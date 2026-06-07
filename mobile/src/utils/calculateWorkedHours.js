export const calculateWorkedHours = (entries) => {
    
    let totalSeconds = 0;
    let startWork = null;

    const sortedEntries = [...entries].sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp));

    for( const entry of sortedEntries){
        const entryTime = new Date(entry.timestamp)

        switch(entry.entryType){
            case 'IN':
                startWork = entryTime;
                console.log('startWork', entry.timestamp)
                break;
            case 'OUT':
                if(startWork){
                    totalSeconds += (entryTime - startWork) / 1000;
                    console.log('endWork', entry.timestamp)
                    console.log(totalSeconds / 3600.0);
                    startWork = null
                }
                break;
        }
    }

    return totalSeconds / 3600.0;
}