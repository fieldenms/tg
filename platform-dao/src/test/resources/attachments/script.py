# Simple Python script to read and print a file's contents

def read_file(filename):
    """Reads a file and prints its contents."""
    try:
        with open(filename, "r", encoding="utf-8") as file:
            content = file.read()
            print(content)
    except FileNotFoundError:
        print(f"Error: The file '{filename}' was not found.")
    except Exception as e:
        print(f"An error occurred: {e}")

# Example usage
if __name__ == "__main__":
    filename = "example.txt"  # Change this to your file name
    read_file(filename)
