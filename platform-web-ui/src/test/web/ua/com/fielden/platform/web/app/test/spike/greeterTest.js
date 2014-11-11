define(['app/testspike/greeter', 'app/testspike/customLib', 'app/testspike/customNonAmdLib'], function(greeter, customLib, CustomNonAmdLib) {
    describe('Greeter module', function () {
        
        it(" must return 'Hello, World' for 'World' parameter.", function() {
            expect(greeter('World')).toBe('Hello, World');
        });

        it(" must return 'Hello, Jhou' for 'Jhou' parameter.", function() {
            expect(greeter('Jhou')).toBe('Hello, Jhou');
        });

        it(" should happen, that trim() function exist.", function() {
        	// alert(typeof String.prototype.trim);
        	// console.log(typeof String.prototype.trim);

            expect(' Jhou '.trim()).toBe('Jhou');
        });

    });
});
