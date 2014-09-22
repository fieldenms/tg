define(['ua/com/fielden/platform/web/app/testspike/greeter'], function(greeter) {
    describe('Greeter suit', function () {
        
        it("Greeter must return 'Hello, World' for 'World' parameter.", function() {
            expect(greeter('World')).toBe('Hello, World');
        });

        it("Greeter must return 'Hello, Jhou' for 'Jhou' parameter.", function() {
            expect(greeter('Jhou')).toBe('Hello, Jhou');
        });
    });
});