#%%
# Importing libraries

import sys
import json
import csv
import pandas as pd
import multiprocessing as mp
import itertools as it

seed_value = 113

# 1. Set the `PYTHONHASHSEED` environment variable at a fixed value
import os
os.environ['PYTHONHASHSEED'] = str(seed_value)

# 2. Set the `python` built-in pseudo-random generator at a fixed value
import random
random.seed(seed_value)

# 3. Set the `numpy` pseudo-random generator at a fixed value
import numpy as np
np.random.seed(seed_value)

# 4. Set the `tensorflow` pseudo-random generator at a fixed value
import tensorflow as tf
tf.random.set_seed(seed_value)

from random import shuffle
from numpy.random import randint
from tensorflow.keras.models import Model
from tensorflow.keras.layers import Input
from tensorflow.keras.layers import BatchNormalization, LayerNormalization
from tensorflow.keras.layers import Dense
from tensorflow.keras.layers import Reshape
from tensorflow.keras.layers import Flatten
from tensorflow.keras.layers import Conv2D
from tensorflow.keras.layers import Conv2DTranspose
from tensorflow.keras.layers import LeakyReLU, ReLU
from tensorflow.keras.layers import Dropout
from tensorflow.keras.layers import Concatenate
from tensorflow.keras.layers import Activation
from tensorflow.keras.initializers import RandomNormal

print("- Importing is completed")

#%%
# Read in parameters

with open(sys.argv[1]) as jsonfile:
    input_settings = json.load(jsonfile)

module_size = 32
recomb_per_module = 16
batch_size = 64
discriminator_iter_num = 5
discriminator_learning_rate = 0.0002
generator_iter_num = 1
generator_learning_rate = 0.0002
n_epochs = 3
gradient_penalty_weight = 10
outpath = input_settings["oOutputPath"]
outname = input_settings["oOutputName"]
    
#%%
# File reading

N100_Ad = pd.read_csv(input_settings["oSourceList"]["oN100_Ad"],
                        delimiter=' ', names = ['SOURCE', 'TARGET'], header = None)

N90_Ad = pd.read_csv(input_settings["oSourceList"]["oN90_Ad"],
                        delimiter=' ', names = ['SOURCE', 'TARGET'], header = None)
N81_Ad = pd.read_csv(input_settings["oSourceList"]["oN81_Ad"],
                        delimiter=' ', names = ['SOURCE', 'TARGET'], header = None)

N90_Mod = pd.read_csv(input_settings["oSourceList"]["oN90_Mod"],
                        delimiter='\t', header = None, index_col= 0)

N81_Mod = pd.read_csv(input_settings["oSourceList"]["oN81_Mod"],
                        delimiter='\t', header = None, index_col= 0)

print("- Reading is completed")

#%%
# Functions for handling the data processing        

def generate_condition_and_linking_set(N_Mod_base, N_Ad_base, N_Ad_plus_1, 
                                        module_th_low, comb_per_module):
    
    n_cpu_worker = mp.cpu_count()

            
    if __name__ == "__main__":

        if (n_cpu_worker >= len(N_Mod_base)):
            n_workers = len(N_Mod_base)
        else:
            n_workers = n_cpu_worker
            
        main_data = []
            
        with mp.Pool(processes = n_workers) as pool:
        
            main_data.extend(pool.starmap(handle_module, zip(it.repeat(N_Mod_base), 
                    it.repeat(N_Ad_base), it.repeat(N_Ad_plus_1), 
                    it.repeat(module_th_low), it.repeat(comb_per_module), range(len(N_Mod_base)))))
                
        flat_list = [item for sublist in main_data for item in sublist]
        
    return flat_list

def handle_module(N_Mod_base, N_Ad_base, N_Ad_plus_1, 
                  module_th_low, comb_per_module, index):
    
    main_data_set  = []
    
    m_size = np.count_nonzero(~np.isnan(N_Mod_base.iloc[index]))
    adjacency_plus_1 = np.zeros((m_size, m_size))
    adjacency_base = np.zeros((m_size, m_size))
    link_ids = np.empty((m_size, m_size), dtype = object)
    
    for j in range(m_size):
        
        for l in range(m_size):

            if (((N_Ad_plus_1["SOURCE"] == int(N_Mod_base.iloc[index].iloc[j])) 
                 & (N_Ad_plus_1["TARGET"] == int(N_Mod_base.iloc[index].iloc[l]))).any()):

                adjacency_plus_1[j, l] = 1
                adjacency_plus_1[l, j] = 1
                
            if (((N_Ad_base["SOURCE"] == int(N_Mod_base.iloc[index].iloc[j])) 
                 & (N_Ad_base["TARGET"] == int(N_Mod_base.iloc[index].iloc[l]))).any()):

                adjacency_base[j, l] = 1
                adjacency_base[l, j] = 1
                
            link_ids[j, l] = str(int(N_Mod_base.iloc[index].iloc[j])) + '_' + str(int(N_Mod_base.iloc[index].iloc[l]))
                
    main_data_set.extend(generate_module_combinations(
        adjacency_base, adjacency_plus_1, link_ids, module_th_low, comb_per_module))
    
    print('processed_modules: %d / %d' % (index+1, len(N_Mod_base)))
        
    return main_data_set
    

def generate_module_combinations(adjacency_base, adjacency_plus_1, 
                                 link_ids, module_th_low, comb_per_module):
    
    all_indeces = list(range(0, np.shape(adjacency_plus_1)[0]))
    combinations = list(it.combinations(all_indeces, module_th_low))
    index_combinations = random.sample(combinations, comb_per_module)
    combinational_data = [None] * len(index_combinations)
        
    for i in range(len(index_combinations)):
        
        adjacency_base_combination = np.take(adjacency_base, index_combinations[i], 0)
        adjacency_base_combination = np.take(adjacency_base_combination, index_combinations[i], 1)
        adjacency_plus_combination = np.take(adjacency_plus_1, index_combinations[i], 0)
        adjacency_plus_combination = np.take(adjacency_plus_combination, index_combinations[i], 1)
        linking_combination = np.take(link_ids, index_combinations[i], 0)
        linking_combination = np.take(linking_combination, index_combinations[i], 1)
        
        combinational_data[i] = [linking_combination, adjacency_base_combination,
                                 adjacency_plus_combination]
        
    return combinational_data

#%%
# Functions for building the Descriminator and the Generator models

def build_discriminator(module_dim):
    
    init = RandomNormal(stddev=0.02)
    
    adjacency_input = Input(shape=(module_dim, module_dim))
    adjacency = Reshape((module_dim, module_dim, 1))(adjacency_input)
    
    linking_input = Input(shape=(module_dim, module_dim))
    linking = Reshape((module_dim, module_dim, 1))(linking_input)
    
    merge = Concatenate()([adjacency, linking])
    
    hidden = Conv2D(64, (4,4), strides=(2,2), padding='same',
                    kernel_initializer=init)(merge)
    hidden = LayerNormalization()(hidden, training=True)
    hidden = LeakyReLU(alpha=0.2)(hidden)

    hidden = Conv2D(128, (4,4), strides=(2,2), padding='same',
                    kernel_initializer=init)(hidden)
    hidden = LayerNormalization()(hidden, training=True)
    hidden = LeakyReLU(alpha=0.2)(hidden)

    hidden = Conv2D(256, (4,4), strides=(2,2), padding='same',
                    kernel_initializer=init)(hidden)
    hidden = LayerNormalization()(hidden, training=True)
    hidden = LeakyReLU(alpha=0.2)(hidden)

    hidden = Conv2D(512, (4,4), strides=(2,2), padding='same',
                    kernel_initializer=init)(hidden)
    hidden = LayerNormalization()(hidden, training=True)
    hidden = LeakyReLU(alpha=0.2)(hidden)

    hidden = Flatten()(hidden)
    hidden = Dropout(0.4)(hidden)
    out_layer = Dense(1, activation='linear')(hidden)

    model = Model([adjacency_input, linking_input], out_layer)
    
    return model

def define_encoder_block(layer_in, n_filters, batchnorm=True):

    init = RandomNormal(stddev=0.02)
    g = Conv2D(n_filters, (4,4), strides=(2,2), padding='same',
               kernel_initializer=init)(layer_in)

    if batchnorm:
        g = LayerNormalization()(g, training=True)

    g = Activation('relu')(g)
    
    return g
 
def decoder_block(layer_in, skip_in, n_filters, dropout=True):

    init = RandomNormal(stddev=0.02)

    g = Conv2DTranspose(n_filters, (4,4), strides=(2,2), padding='same',
                        kernel_initializer=init)(layer_in)
    g = LayerNormalization()(g, training=True)

    if dropout:
        g = Dropout(0.4)(g, training=True)

    g = Concatenate()([g, skip_in])
    g = Activation('relu')(g)
    
    return g

def build_generator(module_dim):

    init = RandomNormal(stddev=0.02)
    
    adjacency_input = Input(shape=(module_dim, module_dim))
    adjacency = Reshape((module_dim, module_dim, 1))(adjacency_input)

    e1 = define_encoder_block(adjacency, 64)
    e2 = define_encoder_block(e1, 128)
    e3 = define_encoder_block(e2, 256)
    e4 = define_encoder_block(e3, 512)

    b = Conv2D(512, (4,4), strides=(2,2), padding='same', kernel_initializer=init)(e4)
    b = Activation('relu')(b)

    d4 = decoder_block(b, e4, 512)
    d5 = decoder_block(d4, e3, 256, dropout=False)
    d6 = decoder_block(d5, e2, 128, dropout=False)
    d7 = decoder_block(d6, e1, 64, dropout=False)

    g = Conv2DTranspose(1, (4,4), strides=(2,2), padding='same', kernel_initializer=init)(d7)
    out_layer = Reshape((module_dim, module_dim))(g)
    out_layer = Activation('sigmoid')(out_layer)
    
    model = Model(adjacency_input, out_layer)
    
    return model

#%%
# Functions for redefining the training and loss calculation

def calculate_discriminator_loss(d_real_output, d_fake_output):

    return tf.reduce_mean(d_fake_output) - tf.reduce_mean(d_real_output)

def calculate_generator_loss(d_fake_output, real_image, fake_image):

    #l1 = tf.reduce_mean(tf.abs(real_image - fake_image))
    #bce = tf.keras.losses.BinaryCrossentropy(reduction=tf.keras.losses.Reduction.SUM)
    #l1 = bce(real_image, fake_image).numpy()
    wasser = -tf.reduce_mean(d_fake_output)
    loss = 1 * wasser

    return loss

def generate_fake_samples(g_model, base_adjacency):        

    linkings = g_model.predict(base_adjacency)

    return linkings 

def calculate_gradient_penalty(d_model, base_adjacency, real_sample, fake_sample, batch_num):

    epsilon = tf.random.uniform(shape=[batch_num, 1, 1], minval = 0, maxval = 1)
    interpolate_sample = epsilon * tf.dtypes.cast(real_sample, tf.float32)
    interpolate_sample = interpolate_sample + ((1 - epsilon) * fake_sample)

    with tf.GradientTape() as gtape:
        
        gtape.watch(interpolate_sample)
        d_interpolate_output = d_model([base_adjacency, interpolate_sample], training=True)
    
    gradients = gtape.gradient(d_interpolate_output, [interpolate_sample])[0]
    norm = tf.sqrt(tf.reduce_sum(tf.square(gradients), axis=[1, 2]))
    gp = tf.reduce_mean((norm - 1.0) ** 2)
    
    return gp

def train_discriminator(d_model, g_model, batch_num, d_iter,
                        base_adjacency, linkings, d_optimizer, gp_weight):
    
    for i in range(d_iter):

        with tf.GradientTape() as gtape:
            
            g_output = g_model(base_adjacency, training = True)
            d_fake_output = d_model([base_adjacency, g_output], training=True)
            d_real_output = d_model([base_adjacency, linkings], training=True)
            
            d_cost = calculate_discriminator_loss(d_real_output, d_fake_output)
            gp = calculate_gradient_penalty(d_model, base_adjacency, linkings, g_output, batch_num)
            d_loss = (d_cost + gp * gp_weight) * 1
            
        d_gradient = gtape.gradient(d_loss, d_model.trainable_variables)
        d_optimizer.apply_gradients(zip(d_gradient, d_model.trainable_variables))    
    
    return d_loss

def train_generator(d_model, g_model, batch_num, g_iter,
                        base_adjacency, linkings, g_optimizer):
    
    for i in range(g_iter):

        with tf.GradientTape() as gtape:
            
            g_output = g_model( base_adjacency, training = True)
            d_fake_output = d_model([base_adjacency, g_output], training=True)
            
            g_loss = calculate_generator_loss(d_fake_output, linkings, g_output)
            
        g_gradient = gtape.gradient(g_loss, g_model.trainable_variables)
        g_optimizer.apply_gradients(zip(g_gradient, g_model.trainable_variables))    
    
    return g_loss

def train_gan(d_model, g_model, epochs_num, data_train, data_test, batch_num, 
              d_iter, g_iter, d_optimizer, g_optimizer, gp_weight):
    
    for epoch in range(epochs_num):
        
        print("- Shuffling train")
        shuffle(data_train)
        print("- Converting train")
        data_train_n = np.asarray(data_train)
        print("- Slicing train")
        base_linkings_train = np.asarray(data_train_n[:, 1, :, :]).astype('float32')
        linkings_train = np.asarray(data_train_n[:, 2, :, :]).astype('float32')
        data_train_n = None
        
        for iteration in range(len(data_train)):
            
            if (iteration * batch_num + batch_num > len(data_train)
                or iteration > 2000):
                break

            adjacency_batch = base_linkings_train[iteration * batch_num : (iteration + 1) * batch_num]
            linking_batch = linkings_train[iteration * batch_num : (iteration + 1) * batch_num]            
        
            d_loss = train_discriminator(d_model, g_model, batch_num, d_iter,
                        adjacency_batch, linking_batch, d_optimizer, gp_weight)
            
            g_loss = train_generator(d_model, g_model, batch_num, g_iter,
                        adjacency_batch, linking_batch, g_optimizer)
	
            print('>%d, %d, d_loss=%.3f, g_loss=%.3f' %(epoch+1, iteration, d_loss, g_loss))
    
    print("- Training is completed")                
    g_model.save(outpath + outname + '_generator.h5')
    generate_results(g_model, data_test)
    
def run_test_batch(g_model, batch_num, conditions_test,
                   base_linkings_test, linkings_test):
    
    index = randint(0, len(base_linkings_test) - batch_num)
    
    linking_batch = linkings_test[index : index + batch_num]
    base_batch = base_linkings_test[index : index + batch_num]
    
    filtered_adjacency_list = []
    filtered_confidency_list = []
    
    test_sample = generate_fake_samples(g_model, base_batch)
    
    for i in range(len(base_batch)):
        
        link_filter = (base_batch[i].flatten()) != 1
        filtered_adjacency_list.extend(((linking_batch[i].flatten())[link_filter]).reshape((-1, 1)))
        filtered_confidency_list.extend(((test_sample[i].flatten())[link_filter]).reshape((-1, 1)))
      
    metric3 = tf.keras.metrics.AUC(num_thresholds=50, curve='ROC')
    metric3.update_state(filtered_adjacency_list, filtered_confidency_list)
    
    return metric3.result().numpy()

def generate_results(g_model, data_test):
    
    print("- Shuffling test")
    shuffle(data_test)
    print("- Converting test")
    data_test_n = np.asarray(data_test)
    print("- Slicing test")
    link_ids_test = np.asarray(data_test_n[:, 0, :, :]).astype('str')
    base_linkings_test = np.asarray(data_test_n[:, 1, :, :]).astype('float32')
    expected_linkings_test = np.asarray(data_test_n[:, 2, :, :]).astype('float32')
    data_test_n = None
    
    filtered_ids_list = []
    filtered_adjacency_list = []
    filtered_confidency_list = []
    
    test_sample = generate_fake_samples(g_model, base_linkings_test)
    
    for i in range(len(base_linkings_test)):
        
        link_filter = (base_linkings_test[i].flatten()) != 1
        filtered_ids_list.extend(((link_ids_test[i].flatten())[link_filter]).reshape((-1, 1)))
        filtered_adjacency_list.extend(((expected_linkings_test[i].flatten())[link_filter]).reshape((-1, 1)))
        filtered_confidency_list.extend(((test_sample[i].flatten())[link_filter]).reshape((-1, 1)))
        
    with open(outpath + outname + "_results.csv","w+") as my_csv:
        csvWriter = csv.writer(my_csv,delimiter='|')
        csvWriter.writerows(zip(
            np.asarray(filtered_ids_list), 
            np.asarray(filtered_adjacency_list), 
            np.asarray(filtered_confidency_list)))

Discriminator = build_discriminator(module_size)
Generator = build_generator(module_size)

print("- GAN building is completed")

disc_opt = tf.keras.optimizers.Adam(learning_rate=discriminator_learning_rate, 
                                    beta_1=0.9, 
                                    beta_2=0.99)

gen_opt = tf.keras.optimizers.Adam(learning_rate=generator_learning_rate, 
                                    beta_1=0.9, 
                                    beta_2=0.99)

train_data = generate_condition_and_linking_set(N81_Mod, N81_Ad, 
                                                N90_Ad, module_size, recomb_per_module)

test_data = generate_condition_and_linking_set(N90_Mod, N90_Ad, 
                                                N100_Ad, module_size, 1)

print("- Data processing is completed")

train_gan(Discriminator, Generator, n_epochs, train_data, test_data, batch_size, 
              discriminator_iter_num, generator_iter_num, disc_opt, gen_opt, gradient_penalty_weight)
