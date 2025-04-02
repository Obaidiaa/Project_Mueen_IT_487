import glob
import os

# Search recursively for .txt files
file_list = sorted(glob.glob("C:/Users/obaid/AndroidStudioProjects/IT_487_Project_3/app/src/main/**", recursive=True))

with open("combined.txt", "w", encoding="utf-8", errors="replace") as outfile:
    for fname in file_list:
        # Only process files
        if not os.path.isfile(fname):
            continue

        # Get the relative path
        rel_path = os.path.relpath(fname, os.getcwd())
        # Split the relative path into parts (directories and file)
        path_parts = rel_path.split(os.sep)

        # Check each folder in the path (excluding the file itself)
        if any(part == "drawable" or "mipmap" in part for part in path_parts[:-1]):
            continue

        try:
            outfile.write("==== " + rel_path + " ====\n")
            with open(fname, "r", encoding="utf-8", errors="replace") as infile:
                outfile.write(infile.read())
            outfile.write("\n\n")  # Optional: adds separation between files
        except PermissionError:
            print(f"Skipping {fname} due to PermissionError")
