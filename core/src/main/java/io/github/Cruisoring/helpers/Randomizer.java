package io.github.Cruisoring.helpers;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class Randomizer {
    public static final String[] lastNames = new String[] {"Lim", "Ahmad", "Siong", "Aw", "Balakrishnan", "Shanmugaratnam", "Batisah", "Bennett", "Ishak", "Casmir", "Chee", "Sing", "Chan", "Seng", "Steve",
            "Chia", "Poh", "Tong", "Chin", "Chong", "Chong", "Chong", "Kim", "Chua", "Chua", "Dhanabalan", "Kuek", "Dong", "Ee", "Wong", "Min", "Tong", "Goh", "Swee", "Chuan", "Gunaratna", "Por", "Haron",
            "Halim", "Hee", "Heng", "Keat", "Ching", "Heng", "Sen", "Rajah", "Jayakumar", "Yanmei", "Hong", "Jeyaretnam", "Jeyaretnam", "Ning", "Wan", "Keng", "Hui", "Puat", "Choo", "Beng", "Lee", "Lee",
            "Wong", "Loong", "Yang", "Min", "Lai", "Chian", "Yew", "Lee", "Lee", "Wee", "Choh", "Jiawei", "Li", "Seng", "Keng", "Lim", "Lim", "Siew", "San", "Hock", "Lim", "Junjie", "Doong", "Lewis", "Long",
            "Khiang", "Lum", "Maidin", "May", "Marshall", "Monteiro", "Nair", "Neo", "Miang", "Hen", "Fong", "Olsen", "Yong", "Ong", "Cheong", "Pang", "Song", "Rajah", "Rajaratnam", "Ramanathan", "Saidi",
            "Sheares", "Sim", "Singh", "Singh", "Chin", "Sreenivasan", "Sun", "Susilo", "Bock", "Tee", "Liang", "Say", "Kee", "Tony", "Lian", "Sye", "Cher", "Tan", "Hian", "Seng", "Lian", "Tarmugi", "Hean",
            "Tjin", "Ling", "Chye", "Yaw", "Wee", "Winsemius", "Seng", "Yang", "Soon", "Yan", "Walter", "Xie", "Mee", "George", "En", "Anandan", "Youyi", "Bala", "Low", "Hartono", "Wong", "Lin", "Lai", "Ng"};

    public static final String[] boyNames = new String[]{"Liam", "Noah", "William", "James", "Logan", "Benjamin", "Mason", "Elijah", "Oliver", "Jacob", "Lucas", "Michael", "Alexander", "Ethan", "Daniel",
            "Matthew", "Aiden", "Henry", "Joseph", "Jackson", "Samuel", "Sebastian", "David", "Carter", "Wyatt", "Jayden", "John", "Owen", "Dylan", "Luke", "Gabriel", "Anthony", "Isaac", "Grayson", "Jack",
            "Julian", "Levi", "Christopher", "Joshua", "Andrew", "Lincoln", "Mateo", "Ryan", "Jaxon", "Nathan", "Aaron", "Isaiah", "Thomas", "Charles", "Caleb", "Josiah", "Christian", "Hunter", "Eli",
            "Jonathan", "Connor", "Landon", "Adrian", "Asher", "Cameron", "Leo", "Theodore", "Jeremiah", "Hudson", "Robert", "Easton", "Nolan", "Nicholas", "Ezra", "Colton", "Angel", "Brayden", "Jordan",
            "Dominic", "Austin", "Ian", "Adam", "Elias", "Jaxson", "Greyson", "Jose", "Ezekiel", "Carson", "Evan", "Maverick", "Bryson", "Jace", "Cooper", "Xavier", "Parker", "Roman", "Jason", "Santiago",
            "Chase", "Sawyer", "Gavin", "Leonardo", "Kayden", "Ayden", "Jameson", "Kevin", "Bentley", "Zachary", "Everett", "Axel", "Tyler", "Micah", "Vincent", "Weston", "Miles", "Wesley", "Nathaniel",
            "Harrison", "Brandon", "Cole", "Declan", "Luis", "Braxton", "Damian", "Silas", "Tristan", "Ryder", "Bennett", "George", "Emmett", "Justin", "Kai", "Max", "Diego", "Luca", "Ryker", "Carlos",
            "Maxwell", "Kingston", "Ivan", "Maddox", "Juan", "Ashton", "Jayce", "Rowan", "Kaiden", "Giovanni", "Eric", "Jesus", "Calvin", "Abel", "King", "Camden", "Amir", "Blake", "Alex", "Brody",
            "Malachi", "Emmanuel", "Jonah", "Beau", "Jude", "Antonio", "Alan", "Elliott", "Elliot", "Waylon", "Xander", "Timothy", "Victor", "Bryce", "Finn", "Brantley", "Edward", "Abraham", "Patrick",
            "Grant", "Karter", "Hayden", "Richard", "Miguel", "Joel", "Gael", "Tucker", "Rhett", "Avery", "Steven", "Graham", "Kaleb", "Jasper", "Jesse", "Matteo", "Dean", "Zayden", "Preston", "August",
            "Oscar", "Jeremy", "Alejandro", "Marcus", "Dawson", "Lorenzo", "Messiah", "Zion", "Maximus", "River", "Zane", "Mark", "Brooks", "Nicolas", "Paxton", "Judah", "Emiliano", "Kaden", "Bryan",
            "Kyle", "Myles", "Peter", "Charlie", "Kyrie", "Thiago", "Brian", "Kenneth", "Andres", "Lukas", "Aidan", "Jax", "Caden", "Milo", "Paul", "Beckett", "Brady", "Colin", "Omar", "Bradley",
            "Javier", "Knox", "Jaden", "Barrett", "Israel", "Matias", "Jorge", "Zander", "Derek", "Josue", "Cayden", "Holden", "Griffin", "Arthur", "Leon", "Felix", "Remington", "Jake", "Killian",
            "Clayton", "Sean", "Adriel", "Riley", "Archer", "Legend", "Erick", "Enzo", "Corbin", "Francisco", "Dallas", "Emilio", "Gunner", "Simon", "Andre", "Walter", "Damien", "Chance", "Phoenix",
            "Colt", "Tanner", "Stephen", "Kameron", "Tobias", "Manuel", "Amari", "Emerson", "Louis", "Cody", "Finley", "Iker", "Martin", "Rafael", "Nash", "Beckham", "Cash", "Karson", "Rylan", "Reid",
            "Theo", "Ace", "Eduardo", "Spencer", "Raymond", "Maximiliano", "Anderson", "Ronan", "Lane", "Cristian", "Titus", "Travis", "Jett", "Ricardo", "Bodhi", "Gideon", "Jaiden", "Fernando", "Mario",
            "Conor", "Keegan", "Ali", "Cesar", "Ellis", "Jayceon", "Walker", "Cohen", "Arlo", "Hector", "Dante", "Kyler", "Garrett", "Donovan", "Seth", "Jeffrey", "Tyson", "Jase", "Desmond", "Caiden",
            "Gage", "Atlas", "Major", "Devin", "Edwin", "Angelo", "Orion", "Conner", "Julius", "Marco", "Jensen", "Daxton", "Peyton", "Zayn", "Collin", "Jaylen", "Dakota", "Prince", "Johnny", "Kayson",
            "Cruz", "Hendrix", "Atticus", "Troy", "Kane", "Edgar", "Sergio", "Kash", "Marshall", "Johnathan", "Romeo", "Shane", "Warren", "Joaquin", "Wade", "Leonel", "Trevor", "Dominick", "Muhammad",
            "Erik", "Odin", "Quinn", "Jaxton", "Dalton", "Nehemiah", "Frank", "Grady", "Gregory", "Andy", "Solomon", "Malik", "Rory", "Clark", "Reed", "Harvey", "Zayne", "Jay", "Jared", "Noel", "Shawn",
            "Fabian", "Ibrahim", "Adonis", "Ismael", "Pedro", "Leland", "Malakai", "Malcolm", "Alexis", "Kason", "Porter", "Sullivan", "Raiden", "Allen", "Ari", "Russell", "Princeton", "Winston",
            "Kendrick", "Roberto", "Lennox", "Hayes", "Finnegan", "Nasir", "Kade", "Nico", "Emanuel", "Landen", "Moises", "Ruben", "Hugo", "Abram", "Adan", "Khalil", "Zaiden", "Augustus", "Marcos",
            "Philip", "Phillip", "Cyrus", "Esteban", "Braylen", "Albert", "Bruce", "Kamden", "Lawson", "Jamison", "Sterling", "Damon", "Gunnar", "Kyson", "Luka", "Franklin", "Ezequiel", "Pablo",
            "Derrick", "Zachariah", "Cade", "Jonas", "Dexter", "Kolton", "Remy", "Hank", "Tate", "Trenton", "Kian", "Drew", "Mohamed", "Dax", "Rocco", "Bowen", "Mathias", "Ronald", "Francis", "Matthias",
            "Milan", "Maximilian", "Royce", "Skyler", "Corey", "Kasen", "Drake", "Gerardo", "Jayson", "Sage", "Braylon", "Benson", "Moses", "Alijah", "Rhys", "Otto", "Oakley", "Armando", "Jaime", "Nixon",
            "Saul", "Scott", "Brycen", "Ariel", "Enrique", "Donald", "Chandler", "Asa", "Eden", "Davis", "Keith", "Frederick", "Rowen", "Lawrence", "Leonidas", "Aden", "Julio", "Darius", "Johan", "Deacon",
            "Cason", "Danny", "Nikolai", "Taylor", "Alec", "Royal", "Armani", "Kieran", "Luciano", "Omari", "Rodrigo", "Arjun", "Ahmed", "Brendan", "Cullen", "Raul", "Raphael", "Ronin", "Brock", "Pierce",
            "Alonzo", "Casey", "Dillon", "Uriel", "Dustin", "Gianni", "Roland", "Landyn", "Kobe", "Dorian", "Emmitt", "Ryland", "Apollo", "Aarav", "Roy", "Duke", "Quentin", "Sam", "Lewis", "Tony", "Uriah",
            "Dennis", "Moshe", "Isaias", "Braden", "Quinton", "Cannon", "Ayaan", "Mathew", "Kellan", "Niko", "Edison", "Izaiah", "Jerry", "Gustavo", "Jamari", "Marvin", "Mauricio", "Ahmad", "Mohammad",
            "Justice", "Trey", "Elian", "Mohammed", "Sincere", "Yusuf", "Arturo", "Callen", "Rayan", "Keaton", "Wilder", "Mekhi", "Memphis", "Cayson", "Conrad", "Kaison", "Kyree", "Soren", "Colby", "Bryant",
            "Lucian", "Alfredo", "Cassius", "Marcelo", "Nikolas", "Brennan", "Darren", "Jasiah", "Jimmy", "Lionel", "Reece", "Ty", "Chris", "Forrest", "Korbin", "Tatum", "Jalen", "Santino", "Case", "Leonard",
            "Alvin", "Issac", "Bo", "Quincy", "Mack", "Samson", "Rex", "Alberto", "Callum", "Curtis", "Hezekiah", "Finnley", "Briggs", "Kamari", "Zeke", "Raylan", "Neil", "Titan", "Julien", "Kellen", "Devon",
            "Kylan", "Roger", "Axton", "Carl", "Douglas", "Larry", "Crosby", "Fletcher", "Makai", "Nelson", "Hamza", "Lance", "Alden", "Gary", "Wilson", "Alessandro", "Ares", "Kashton", "Bruno", "Jakob",
            "Stetson", "Zain", "Cairo", "Nathanael", "Byron", "Harry", "Harley", "Mitchell", "Maurice", "Orlando", "Kingsley", "Kaysen", "Sylas", "Trent", "Ramon", "Boston", "Lucca", "Noe", "Jagger", "Reyansh",
            "Vihaan", "Randy", "Thaddeus", "Lennon", "Kannon", "Kohen", "Tristen", "Valentino", "Maxton", "Salvador", "Abdiel", "Langston", "Rohan", "Kristopher", "Yosef", "Rayden", "Lee", "Callan", "Tripp",
            "Deandre", "Joe", "Morgan", "Dariel", "Colten", "Reese", "Jedidiah", "Ricky", "Bronson", "Terry", "Eddie", "Jefferson", "Lachlan", "Layne", "Clay", "Madden", "Jamir", "Tomas", "Kareem", "Stanley",
            "Brayan", "Amos", "Kase", "Kristian", "Clyde", "Ernesto", "Tommy", "Casen", "Ford", "Crew", "Braydon", "Brecken", "Hassan", "Axl", "Boone", "Leandro", "Samir", "Jaziel", "Magnus", "Abdullah",
            "Yousef", "Branson", "Jadiel", "Jaxen", "Layton", "Franco", "Ben", "Grey", "Kelvin", "Chaim", "Demetrius", "Blaine", "Ridge", "Colson", "Melvin", "Anakin", "Aryan", "Lochlan", "Jon", "Canaan",
            "Dash", "Zechariah", "Alonso", "Otis", "Zaire", "Marcel", "Brett", "Stefan", "Aldo", "Jeffery", "Baylor", "Talon", "Dominik", "Flynn", "Carmelo", "Dane", "Jamal", "Kole", "Enoch", "Graysen",
            "Kye", "Vicente", "Fisher", "Ray", "Fox", "Jamie", "Rey", "Zaid", "Allan", "Emery", "Gannon", "Joziah", "Rodney", "Juelz", "Sonny", "Terrance", "Zyaire", "Augustine", "Cory", "Felipe", "Aron",
            "Jacoby", "Harlan", "Marc", "Bobby", "Joey", "Anson", "Huxley", "Marlon", "Anders", "Guillermo", "Payton", "Castiel", "Damari", "Shepherd", "Azariah", "Harold", "Harper", "Henrik", "Houston",
            "Kairo", "Willie", "Elisha", "Ameer", "Emory", "Skylar", "Sutton", "Alfonso", "Brentley", "Toby", "Blaze", "Eugene", "Shiloh", "Wayne", "Darian", "Gordon", "London", "Bodie", "Jordy", "Jermaine",
            "Denver", "Gerald", "Merrick", "Musa", "Vincenzo", "Kody", "Yahir", "Brodie", "Trace", "Darwin", "Tadeo", "Bentlee", "Billy", "Hugh", "Reginald", "Vance", "Westin", "Cain", "Arian", "Dayton",
            "Javion", "Terrence", "Brysen", "Jaxxon", "Thatcher", "Landry", "Rene", "Westley", "Miller", "Alvaro", "Cristiano", "Eliseo", "Ephraim", "Adrien", "Jerome", "Khalid", "Aydin", "Mayson", "Alfred",
            "Duncan", "Junior", "Kendall", "Zavier", "Koda", "Maison", "Caspian", "Maxim", "Kace", "Zackary", "Rudy", "Coleman", "Keagan", "Kolten", "Maximo", "Dario", "Davion", "Kalel", "Briar", "Jairo",
            "Misael", "Rogelio", "Terrell", "Heath", "Micheal", "Wesson", "Aaden", "Brixton", "Draven", "Xzavier", "Darrell", "Keanu", "Ronnie", "Konnor", "Will", "Dangelo", "Frankie", "Kamryn", "Salvatore",
            "Santana", "Shaun", "Coen", "Leighton", "Mustafa", "Reuben", "Ayan", "Blaise", "Dimitri", "Keenan", "Van", "Achilles", "Channing", "Ishaan", "Wells", "Benton", "Lamar", "Nova", "Yahya", "Dilan",
            "Gibson", "Camdyn", "Ulises", "Alexzander", "Valentin", "Shepard", "Alistair", "Eason", "Kaiser", "Leroy", "Zayd", "Camilo", "Markus", "Foster", "Davian", "Dwayne", "Jabari", "Judson", "Koa",
            "Yehuda", "Lyric", "Tristian", "Agustin", "Bridger", "Vivaan", "Brayson", "Emmet", "Marley", "Mike", "Nickolas", "Kenny", "Leif", "Bjorn", "Ignacio", "Rocky", "Chad", "Gatlin", "Greysen", "Kyng",
            "Randall", "Reign", "Vaughn", "Jessie", "Louie", "Shmuel", "Zahir", "Ernest", "Javon", "Khari", "Reagan", "Avi", "Ira", "Ledger", "Simeon", "Yadiel", "Maddux", "Seamus", "Jad", "Jeremias", "Kylen",
            "Rashad", "Santos", "Cedric", "Craig", "Dominique", "Gianluca", "Jovanni", "Bishop", "Brenden", "Anton", "Camron", "Giancarlo", "Lyle", "Alaric", "Decker", "Eliezer", "Ramiro", "Yisroel", "Howard", "Jaxx"};
    public static final String[] girlNames = new String[]{"Emma", "Olivia", "Ava", "Isabella", "Sophia", "Mia", "Charlotte", "Amelia", "Evelyn", "Abigail", "Harper", "Emily", "Elizabeth", "Avery", "Sofia", "Ella", "Madison", "Scarlett", "Victoria", "Aria",
            "Grace", "Chloe", "Camila", "Penelope", "Riley", "Layla", "Lillian", "Nora", "Zoey", "Mila", "Aubrey", "Hannah", "Lily", "Addison", "Eleanor", "Natalie", "Luna", "Savannah", "Brooklyn", "Leah", "Zoe",
            "Stella", "Hazel", "Ellie", "Paisley", "Audrey", "Skylar", "Violet", "Claire", "Bella", "Aurora", "Lucy", "Anna", "Samantha", "Caroline", "Genesis", "Aaliyah", "Kennedy", "Kinsley", "Allison",
            "Maya", "Sarah", "Madelyn", "Adeline", "Alexa", "Ariana", "Elena", "Gabriella", "Naomi", "Alice", "Sadie", "Hailey", "Eva", "Emilia", "Autumn", "Quinn", "Nevaeh", "Piper", "Ruby", "Serenity",
            "Willow", "Everly", "Cora", "Kaylee", "Lydia", "Aubree", "Arianna", "Eliana", "Peyton", "Melanie", "Gianna", "Isabelle", "Julia", "Valentina", "Nova", "Clara", "Vivian", "Reagan", "Mackenzie",
            "Madeline", "Brielle", "Delilah", "Isla", "Rylee", "Katherine", "Sophie", "Josephine", "Ivy", "Liliana", "Jade", "Maria", "Taylor", "Hadley", "Kylie", "Emery", "Adalynn", "Natalia", "Annabelle",
            "Faith", "Alexandra", "Ximena", "Ashley", "Brianna", "Raelynn", "Bailey", "Mary", "Athena", "Andrea", "Leilani", "Jasmine", "Lyla", "Margaret", "Alyssa", "Adalyn", "Arya", "Norah", "Khloe", "Kayla",
            "Eden", "Eliza", "Rose", "Ariel", "Melody", "Alexis", "Isabel", "Sydney", "Juliana", "Lauren", "Iris", "Emerson", "London", "Morgan", "Lilly", "Charlie", "Aliyah", "Valeria", "Arabella", "Sara", "Finley",
            "Trinity", "Ryleigh", "Jordyn", "Jocelyn", "Kimberly", "Esther", "Molly", "Valerie", "Cecilia", "Anastasia", "Daisy", "Reese", "Laila", "Mya", "Amy", "Teagan", "Amaya", "Elise", "Harmony", "Paige",
            "Adaline", "Fiona", "Alaina", "Nicole", "Genevieve", "Lucia", "Alina", "Mckenzie", "Callie", "Payton", "Eloise", "Brooke", "Londyn", "Mariah", "Julianna", "Rachel", "Daniela", "Gracie",
            "Catherine", "Angelina", "Presley", "Josie", "Harley", "Adelyn", "Vanessa", "Makayla", "Parker", "Juliette", "Amara", "Marley", "Lila", "Ana", "Rowan", "Alana", "Michelle", "Malia", "Rebecca",
            "Brooklynn", "Brynlee", "Summer", "Sloane", "Leila", "Sienna", "Adriana", "Sawyer", "Kendall", "Juliet", "Destiny", "Alayna", "Elliana", "Diana", "Hayden", "Ayla", "Dakota", "Angela", "Noelle",
            "Rosalie", "Joanna", "Jayla", "Alivia", "Lola", "Emersyn", "Georgia", "Selena", "June", "Daleyza", "Tessa", "Maggie", "Jessica", "Remi", "Delaney", "Camille", "Vivienne", "Hope", "Mckenna", "Gemma",
            "Olive", "Alexandria", "Blakely", "Izabella", "Catalina", "Raegan", "Journee", "Gabrielle", "Lucille", "Ruth", "Amiyah", "Evangeline", "Blake", "Thea", "Amina", "Giselle", "Lilah", "Melissa",
            "River", "Kate", "Adelaide", "Charlee", "Vera", "Leia", "Gabriela", "Zara", "Jane", "Journey", "Elaina", "Miriam", "Briella", "Stephanie", "Cali", "Ember", "Lilliana", "Aniyah", "Logan", "Kamila",
            "Brynn", "Ariella", "Makenzie", "Annie", "Mariana", "Kali", "Haven", "Elsie", "Nyla", "Paris", "Lena", "Freya", "Adelynn", "Lyric", "Camilla", "Sage", "Jennifer", "Paislee", "Talia", "Alessandra",
            "Juniper", "Fatima", "Raelyn", "Amira", "Arielle", "Phoebe", "Kinley", "Ada", "Nina", "Ariah", "Samara", "Myla", "Brinley", "Cassidy", "Maci", "Aspen", "Allie", "Keira", "Kaia", "Makenna", "Amanda",
            "Heaven", "Joy", "Lia", "Madilyn", "Gracelyn", "Laura", "Evelynn", "Lexi", "Haley", "Miranda", "Kaitlyn", "Daniella", "Felicity", "Jacqueline", "Evie", "Angel", "Danielle", "Ainsley", "Dylan",
            "Kiara", "Millie", "Jordan", "Maddison", "Rylie", "Alicia", "Maeve", "Margot", "Kylee", "Phoenix", "Heidi", "Zuri", "Alondra", "Lana", "Madeleine", "Gracelynn", "Kenzie", "Miracle", "Shelby",
            "Elle", "Adrianna", "Bianca", "Addilyn", "Kira", "Veronica", "Gwendolyn", "Esmeralda", "Chelsea", "Alison", "Skyler", "Magnolia", "Daphne", "Jenna", "Everleigh", "Kyla", "Braelynn", "Harlow",
            "Annalise", "Mikayla", "Dahlia", "Maliyah", "Averie", "Scarlet", "Kayleigh", "Luciana", "Kelsey", "Nadia", "Amber", "Gia", "Kamryn", "Yaretzi", "Carmen", "Jimena", "Erin", "Christina", "Katie",
            "Ryan", "Viviana", "Alexia", "Anaya", "Serena", "Katelyn", "Ophelia", "Regina", "Helen", "Remington", "Camryn", "Cadence", "Royalty", "Amari", "Kathryn", "Skye", "Emely", "Jada", "Ariyah", "Aylin",
            "Saylor", "Kendra", "Cheyenne", "Fernanda", "Sabrina", "Francesca", "Eve", "Mckinley", "Frances", "Sarai", "Carolina", "Kennedi", "Nylah", "Tatum", "Alani", "Lennon", "Raven", "Zariah", "Leslie",
            "Winter", "Abby", "Mabel", "Sierra", "April", "Willa", "Carly", "Jolene", "Rosemary", "Aviana", "Madelynn", "Selah", "Renata", "Lorelei", "Briana", "Celeste", "Wren", "Charleigh", "Leighton",
            "Annabella", "Jayleen", "Braelyn", "Ashlyn", "Jazlyn", "Mira", "Oakley", "Malaysia", "Edith", "Avianna", "Maryam", "Emmalyn", "Hattie", "Kensley", "Macie", "Bristol", "Marlee", "Demi",
            "Cataleya", "Maia", "Sylvia", "Itzel", "Allyson", "Lilith", "Melany", "Kaydence", "Holly", "Nayeli", "Meredith", "Nia", "Liana", "Megan", "Justice", "Bethany", "Alejandra", "Janelle",
            "Elisa", "Adelina", "Ashlynn", "Elianna", "Aleah", "Myra", "Lainey", "Blair", "Kassidy", "Charley", "Virginia", "Kara", "Helena", "Sasha", "Julie", "Michaela", "Carter", "Matilda", "Kehlani",
            "Henley", "Maisie", "Hallie", "Jazmin", "Priscilla", "Marilyn", "Cecelia", "Danna", "Colette", "Baylee", "Elliott", "Ivanna", "Cameron", "Celine", "Alayah", "Hanna", "Imani", "Angelica",
            "Emelia", "Kalani", "Alanna", "Lorelai", "Macy", "Karina", "Addyson", "Aleena", "Aisha", "Johanna", "Mallory", "Leona", "Mariam", "Kynlee", "Madilynn", "Karen", "Karla", "Skyla", "Beatrice",
            "Dayana", "Gloria", "Milani", "Savanna", "Karsyn", "Rory", "Giuliana", "Lauryn", "Liberty", "Galilea", "Aubrie", "Charli", "Kyleigh", "Brylee", "Jillian", "Anne", "Haylee", "Dallas", "Azalea",
            "Jayda", "Tiffany", "Avah", "Shiloh", "Bailee", "Jazmine", "Esme", "Coraline", "Madisyn", "Elaine", "Lilian", "Kyra", "Kaliyah", "Kora", "Octavia", "Irene", "Kelly", "Lacey", "Laurel", "Adley",
            "Anika", "Janiyah", "Related Post", "BELLISSIMA! 14 BEAUTIFUL ITALIAN GIRL NAMES", "Dorothy", "Sutton", "Julieta", "Kimber", "Remy", "Cassandra", "Rebekah", "Collins",
            "Elliot", "Emmy", "Sloan", "Hayley", "Amalia", "Jemma", "Jamie", "Melina", "Leyla", "Jaylah", "Anahi", "Jaliyah", "Kailani", "Harlee", "Wynter", "Saige", "Alessia", "Monica", "Anya", "Antonella",
            "Emberly", "Khaleesi", "Ivory", "Greta", "Maren", "Alena", "Emory", "Alaia", "Cynthia", "Addisyn", "Alia", "Lylah", "Angie", "Ariya", "Alma", "Crystal", "Jayde", "Aileen", "Kinslee", "Siena",
            "Zelda", "Katalina", "Marie", "Pearl", "Reyna", "Mae", "Zahra", "Kailey", "Jessie", "Tiana", "Amirah", "Madalyn", "Alaya", "Lilyana", "Julissa", "Armani", "Lennox", "Lillie", "Jolie", "Laney",
            "Roselyn", "Mara", "Joelle", "Rosa", "Kaylani", "Bridget", "Liv", "Oaklyn", "Aurelia", "Clarissa", "Elyse", "Marissa", "Monroe", "Kori", "Elsa", "Rosie", "Amelie", "Aitana", "Aliza", "Eileen",
            "Poppy", "Emmie", "Braylee", "Milana", "Addilynn", "Royal", "Chaya", "Frida", "Bonnie", "Amora", "Stevie", "Tatiana", "Malaya", "Mina", "Emerie", "Reign", "Zaylee", "Annika", "Kenia", "Linda",
            "Kenna", "Faye", "Reina", "Brittany", "Marina", "Astrid", "Kadence", "Mikaela", "Jaelyn", "Briar", "Kaylie", "Teresa", "Bria", "Hadassah", "Lilianna", "Guadalupe", "Rayna", "Chanel", "Lyra",
            "Noa", "Zariyah", "Laylah", "Aubrielle", "Aniya", "Livia", "Ellen", "Meadow", "Amiya", "Ellis", "Elora", "Milan", "Hunter", "Princess", "Leanna", "Nathalie", "Clementine", "Nola", "Tenley",
            "Simone", "Lina", "Marianna", "Martha", "Sariah", "Louisa", "Noemi", "Emmeline", "Kenley", "Belen", "Erika", "Myah", "Lara", "Amani", "Ansley", "Everlee", "Maleah", "Salma", "Jaelynn", "Kiera",
            "Dulce", "Nala", "Natasha", "Averi", "Mercy", "Penny", "Ariadne", "Deborah", "Elisabeth", "Zaria", "Hana", "Kairi", "Yareli", "Raina", "Ryann", "Lexie", "Thalia", "Karter", "Annabel", "Christine",
            "Estella", "Keyla", "Adele", "Aya", "Estelle", "Landry", "Tori", "Perla", "Lailah", "Miah", "Rylan", "Angelique", "Avalynn", "Romina", "Ari", "Jaycee", "Jaylene", "Kai", "Louise", "Mavis", "Scarlette",
            "Belle", "Lea", "Nalani", "Rivka", "Ayleen", "Calliope", "Dalary", "Zaniyah", "Kaelyn", "Sky", "Jewel", "Joselyn", "Madalynn", "Paola", "Giovanna", "Isabela", "Karlee", "Aubriella", "Azariah",
            "Tinley", "Dream", "Claudia", "Corinne", "Erica", "Milena", "Aliana", "Kallie", "Alyson", "Joyce", "Tinsley", "Whitney", "Emilee", "Paisleigh", "Carolyn", "Jaylee", "Zoie", "Frankie", "Andi",
            "Judith", "Paula", "Xiomara", "Aiyana", "Amia", "Analia", "Audrina", "Hadlee", "Rayne", "Amayah", "Cara", "Celia", "Lyanna", "Opal", "Amaris", "Clare", "Gwen", "Giana", "Veda", "Alisha", "Davina",
            "Rhea", "Sariyah", "Noor", "Danica", "Kathleen", "Lillianna", "Lindsey", "Maxine", "Paulina", "Hailee", "Harleigh", "Nancy", "Jessa", "Raquel", "Raylee", "Zainab", "Chana", "Lisa", "Heavenly",
            "Oaklynn", "Aminah", "Emmalynn", "Patricia", "India", "Janessa", "Paloma", "Ramona", "Sandra", "Abril", "Emmaline", "Itzayana", "Kassandra", "Vienna", "Marleigh", "Kailyn", "Novalee",
            "Rosalyn", "Hadleigh", "Luella", "Taliyah", "Avalyn", "Barbara", "Iliana", "Jana", "Meilani", "Aadhya", "Alannah", "Blaire", "Brenda", "Casey", "Selene", "Lizbeth", "Adrienne", "Annalee",
            "Malani", "Aliya", "Miley", "Nataly", "Bexley", "Joslyn", "Maliah", "Zion", "Breanna", "Melania", "Estrella", "Ingrid", "Jayden", "Kaya", "Kaylin", "Harmoni", "Arely", "Jazlynn", "Kiana", "Dana",
            "Mylah", "Oaklee", "Ailani", "Kailee", "Legacy", "Marjorie", "Paityn", "Courtney", "Ellianna", "Jurnee", "Karlie", "Evalyn", "Holland", "Kenya", "Magdalena", "Carla", "Halle", "Aryanna", "Kaiya",
            "Kimora", "Naya", "Saoirse", "Susan", "Desiree", "Ensley", "Renee", "Esperanza", "Treasure", "Caylee", "Ellison", "Kristina", "Adilynn", "Anabelle", "Egypt", "Spencer", "Tegan", "Aranza", "Vada",
            "Emerald", "Florence", "Marlowe", "Micah", "Sonia", "Sunny", "Tara", "Riya", "Yara", "Alisa", "Nathalia", "Yamileth", "Saanvi", "Samira", "Sylvie", "Brenna", "Carlee", "Jenny", "Miya", "Monserrat",
            "Zendaya", "Alora"};

    public static Integer[] ages = new Integer[]{9, 9, 10,10, 10, 11, 11, 11, 11, 12, 12, 12, 12, 12, 12, 13, 13, 13, 13, 13, 14, 14, 14, 14, 14, 15, 15, 15, 15, 15, 15, 15,
            16, 16, 16, 16, 16, 16, 16, 16, 17, 17, 17, 17, 17, 17, 18, 18, 18, 18, 18, 19, 19, 19, 20, 20, 20, 23, 25, 27, 33, 45, 50, 63
        };

    public static Integer[] playerNumbers = new Integer[]{1, 2, 3, 3, 3, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 9, 9, 9, 9, 9, 9, 9, 9, 10, 10, 10, 10, 10, 10, 11, 11, 11, 11, 12, 12, 12, 12, 12, 12, 12, 12, 12, 13, 15
    };

    static File proxyFile = new File("c:/working/proxies.txt");

    public static Proxy[] proxies = null;

    static {
        if(proxyFile.exists()){
            List<String> lines = new ArrayList<>();
            List<Proxy> list = new ArrayList<>();
            try (Stream<String> stream = Files.lines(proxyFile.toPath())) {

                stream.forEach(lines::add);

                for (String line : lines) {
                    String address = line.substring(line.indexOf("/")+1, line.indexOf(":"));
                    int port =  Integer.valueOf(line.substring(line.indexOf(":")+1));

                    Proxy proxy = line.startsWith("HTTP") ? URLHelper.getHttpProxy(address, port) : URLHelper.getSocketProxy(address, port);
                    list.add(proxy);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                proxies = list.toArray(new Proxy[0]);
            }

        }
    }

    public static final Random random = new Random();

    /**
     * Get a random element of the given array.
     * @param array Array containing multiple elements
     * @param <T>   Type of the element
     * @return      An element randomly fetched from the arry
     */
    public static <T> T getRandom(T[] array){
        int size = array.length;
        int index = random.nextInt(size);
        return array[index];
    }

    public static Proxy getProxy(){
        return getRandom(proxies);
    }

    public static Float round(Float number, int scale) {
        int pow = 10;
        for (int i = 1; i < scale; i++)
            pow *= 10;
        float tmp = number * pow;
        return ( (float) ( (int) ((tmp - (int) tmp) >= 0.5f ? tmp + 1 : tmp) ) ) / pow;
    }

    public static Float getRandomFloat(Float min, Float max){
        Float delta = max - min;
        Float ratio = random.nextFloat();
        Float result = round(delta*ratio + min,2);
        return result;
    }

    public static String getRandomName(String gender){
        String familyName = getRandom(lastNames);
        if("Femail".equalsIgnoreCase(gender) || "F".equalsIgnoreCase(gender)){
            return familyName + ", " + getRandom(girlNames);
        } else {
            return familyName + ", " + getRandom(boyNames);
        }
    }

    public static Integer getRandomAge(){
        return getRandom(ages);
    }
}
