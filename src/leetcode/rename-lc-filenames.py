
import os
import re

# Get the current working directory
folder_path = os.getcwd()

# Define a regular expression pattern to match the filenames
pattern = r"leetcode-(\d+)-(.+)"

# Define a regular expression pattern to match the filenames
pattern1 = r"leetcode-(\d+)-(.+)\.md"
pattern2 = r"leetcode-(\d+)\.md"

# Iterate over all files in the folder
for filename in os.listdir(folder_path):
    match1 = re.match(pattern1, filename)
    match2 = re.match(pattern2, filename)
    
    if match1:
        number_part, text_part = match1.groups()
        # Pad the number part to 4 digits
        new_number_part = number_part.zfill(4)
        # Construct the new filename
        new_filename = f"leetcode-{new_number_part}-{text_part}.md"
    elif match2:
        number_part = match2.group(1)
        # Pad the number part to 4 digits
        new_number_part = number_part.zfill(4)
        # Construct the new filename
        new_filename = f"leetcode-{new_number_part}.md"
    else:
        continue  # Skip files that don't match either pattern

    # Construct the full file paths
    old_filepath = os.path.join(folder_path, filename)
    new_filepath = os.path.join(folder_path, new_filename)

    # Rename the file
    os.rename(old_filepath, new_filepath)
    print(f"Renamed: {filename} -> {new_filename}")