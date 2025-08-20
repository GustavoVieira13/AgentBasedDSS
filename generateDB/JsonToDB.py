import mysql.connector
import json
import random

# Configuração para a conexão com o banco de dados MySQL
config = {
    'user': 'root',                  # Seu usuário MySQL
    'password': '@MYSQL123',         # Sua senha MySQL
    'host': 'localhost',             # Endereço do servidor (localhost, se for local)
    'port': 3306,                    # Porta do MySQL (3306 é a padrão)
    'database': 'workloadbalance'    # Nome do banco de dados
}

# Conexão com o MySQL
conexao = mysql.connector.connect(**config)
cursor = conexao.cursor()

def criar_tabelas():

    cursor.execute("DROP TABLE IF EXISTS worker_experience_on_the_line")
    cursor.execute("DROP TABLE IF EXISTS worker_job_rotation")
    cursor.execute("DROP TABLE IF EXISTS worker_preference")
    cursor.execute("DROP TABLE IF EXISTS workers")
    
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS workers (
            Id VARCHAR(10) PRIMARY KEY,
            Availability BOOLEAN,
            MedicalCondition BOOLEAN,
            UTEExperience BOOLEAN,
            WorkerResilience FLOAT,
            Gender VARCHAR(6),        
            Age INT                   
        )
    """)

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS worker_preference (
            WorkerId VARCHAR(10),
            LineId VARCHAR(10),
            Value FLOAT,
            FOREIGN KEY (WorkerId) REFERENCES workers(Id) ON DELETE CASCADE
        )
    """)

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS worker_job_rotation (
            WorkerId VARCHAR(10),
            LineId VARCHAR(10),
            Value FLOAT,
            FOREIGN KEY (WorkerId) REFERENCES workers(Id) ON DELETE CASCADE
        )
    """)

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS worker_experience_on_the_line (
            WorkerId VARCHAR(10),
            LineId VARCHAR(10),
            Value INT,
            FOREIGN KEY (WorkerId) REFERENCES workers(Id) ON DELETE CASCADE
        )
    """)

def gerar_experience_e_job_rotation():
    experience = random.randint(0, 10)
    job_rotation = 0.0 if experience == 0 else round((experience / 10) * 50, 2)
    return experience, job_rotation

def gerar_genero_e_idade():
    gender = random.choice(["Male", "Female"])
    age = random.randint(18, 60)
    return gender, age

def inserir_dados(dados):
    for order in dados["OrderInfoList"]:
        for worker in order["WorkerInfoList"]:
            # Gerar valores aleatórios para Gender e Age
            gender, age = gerar_genero_e_idade()

            # Inserir dados na tabela `workers`
            cursor.execute("""
                INSERT INTO workers (Id, Availability, MedicalCondition, UTEExperience, WorkerResilience, Gender, Age)
                VALUES (%s, %s, %s, %s, %s, %s, %s)
                ON DUPLICATE KEY UPDATE
                    Availability=VALUES(Availability),
                    MedicalCondition=VALUES(MedicalCondition),
                    UTEExperience=VALUES(UTEExperience),
                    WorkerResilience=VALUES(WorkerResilience),
                    Gender=VALUES(Gender),
                    Age=VALUES(Age)
            """, (
                worker["Id"],
                worker["Availability"] == "True",
                worker["MedicalCondition"] == "True",
                worker["UTEExperience"] == "True",
                worker["WorkerResilience"],
                gender,
                age
            ))

            # Inserir dados na tabela `worker_preference`
            for preference in worker["WorkerPreference"]:
                cursor.execute("""
                    INSERT INTO worker_preference (WorkerId, LineId, Value)
                    VALUES (%s, %s, %s)
                    ON DUPLICATE KEY UPDATE Value=VALUES(Value)
                """, (
                    worker["Id"],
                    preference["LineId"],
                    preference["Value"]
                ))

            # Inserir dados nas tabelas `worker_job_rotation` e `worker_experience_on_the_line`
            for line_id in ["17", "18", "20"]:
                experience, job_rotation = gerar_experience_e_job_rotation()
                
                # Inserir ExperienceOnTheLine
                cursor.execute("""
                    INSERT INTO worker_experience_on_the_line (WorkerId, LineId, Value)
                    VALUES (%s, %s, %s)
                    ON DUPLICATE KEY UPDATE Value=VALUES(Value)
                """, (
                    worker["Id"],
                    line_id,
                    experience
                ))

                # Inserir JobRotation com duas casas decimais
                cursor.execute("""
                    INSERT INTO worker_job_rotation (WorkerId, LineId, Value)
                    VALUES (%s, %s, %s)
                    ON DUPLICATE KEY UPDATE Value=VALUES(Value)
                """, (
                    worker["Id"],
                    line_id,
                    job_rotation
                ))

# Carregar e processar o JSON
with open("dados.json", "r") as arquivo_json:
    dados = json.load(arquivo_json)

# Executar funções de criação de tabela e inserção de dados
criar_tabelas()
inserir_dados(dados)

# Confirmar a inserção e fechar a conexão
conexao.commit()
cursor.close()
conexao.close()
