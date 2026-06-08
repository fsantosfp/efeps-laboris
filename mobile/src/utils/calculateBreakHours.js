export const calculateBreakHours = (entries) => {
    let totalSeconds = 0;
    let lastOut = null;

    const sortedEntries = [...entries].sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp));

    for (const entry of sortedEntries) {
        const entryTime = new Date(entry.timestamp);

        switch (entry.entryType) {
            case 'OUT':
                lastOut = entryTime;
                break;
            case 'IN':
                if (lastOut) {
                    const outDate = lastOut.toISOString().split('T')[0];
                    const inDate = entryTime.toISOString().split('T')[0];
                    if (outDate === inDate) {
                        totalSeconds += (entryTime - lastOut) / 1000;
                    }
                    lastOut = null;
                }
                break;
        }
    }

    return totalSeconds / 3600.0;
};
