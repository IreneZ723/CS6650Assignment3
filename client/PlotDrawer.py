import pandas as pd
import matplotlib

matplotlib.use('TkAgg')
import matplotlib.pyplot as plt

# Load your CSV file into a Pandas DataFrame
df = pd.read_csv('latency_rcds-Java-30.csv')

# Convert the startTime to seconds (assuming the startTime is in milliseconds)

# Sort the DataFrame by startTime
df = df.sort_values(by='startTime')
startTime = df['startTime'].min()
lastStartTime = df['startTime'].max()

# Calculate the number of seconds to cover the entire test duration
num_seconds = int((lastStartTime - startTime) / 1000) + 1

# Calculate the test wall time in seconds
# test_wall_time = int(df['startTime'].iloc[-1])

# Create a list of time intervals in seconds from 0 to test_wall_time
time_intervals = list(range(0, num_seconds))
print(time_intervals);
# Calculate throughput for each second
throughput = []
for interval in time_intervals:
    requests_in_interval = (
            (df['startTime'] >= startTime + interval * 1000) & (
            (df['startTime']) <= startTime + (interval + 1) * 1000)).sum()
    throughput.append(requests_in_interval)

print(throughput);
# Create a chart
plt.figure(figsize=(12, 6))
plt.plot(time_intervals, throughput)
plt.xlabel('Time (seconds)')
plt.ylabel('Throughput/second')
plt.title('Request Throughput Over Time')
plt.grid(True)

# Show the chart
plt.show()
