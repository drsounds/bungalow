class Field {
    constructor(field) {
        let parts = field.split(':');
        this.id = parts[0];
        if (parts.length < 1) {
            parts.push('String');
        }
        this.type = parts[1];
        this.name = parts[0];
    }
}